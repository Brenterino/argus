package dev.zygon.argus.location;

import dev.zygon.argus.location.client.LocationsClients.AliceClient;
import dev.zygon.argus.location.client.LocationsClients.BobClient;
import dev.zygon.argus.location.client.LocationsClients.ExpiredTokenClient;
import dev.zygon.argus.user.User;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static dev.zygon.argus.location.client.LocationsClients.ALICE_RECEIVED;
import static dev.zygon.argus.location.client.LocationsClients.BOB_RECEIVED;
import static org.assertj.core.api.Assertions.*;

@Slf4j
@QuarkusTest
@TestProfile(IntegrationProfile.class)
public class LocationsSocketJwtExpirationIT {

    @TestHTTPResource("/locations")
    private URI uri;

    private Session alice;
    private Session bob;

    @BeforeEach
    public void setUp() throws Exception {
        alice = ContainerProvider.getWebSocketContainer()
                .connectToServer(AliceClient.class, uri);
        bob = ContainerProvider.getWebSocketContainer()
                .connectToServer(BobClient.class, uri);
    }

    @Test
    public void sessionCannotBeOpenedIfJwtIsExpired() {
        setMaxStackTraceElementsDisplayed(1000);
        assertThatThrownBy(() -> {
            try (var expired = ContainerProvider.getWebSocketContainer()
                    .connectToServer(ExpiredTokenClient.class, uri)) {
                // should never reach here
            }
        })
                .isNotNull()
                .extracting(Throwable::getSuppressed)
                .asInstanceOf(InstanceOfAssertFactories.ARRAY)
                .singleElement()
                .asInstanceOf(InstanceOfAssertFactories.THROWABLE)
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isInstanceOf(WebSocketClientHandshakeException.class)
                .hasFieldOrPropertyWithValue("message", "Invalid handshake response getStatus: 401 Unauthorized");
    }

    @Test
    public void sessionCanWriteBeforeAndAfterTokenExpiration() throws Exception {
        var userAlice = new User("1", "Alice");
        var userBob = new User("2", "Bob");

        var locationAlice = new Location(101, 50, 2100, 0, true, Instant.now());
        var userLocationAlice = new UserLocation(userAlice, locationAlice);
        var aliceLocations = new Locations(Set.of(userLocationAlice));

        var locationBob = new Location(-100, 0, -100, 0, true, Instant.now());
        var userLocationBob = new UserLocation(userBob, locationBob);
        var bobLocations = new Locations(Set.of(userLocationBob));

        alice.getAsyncRemote()
                .sendObject(aliceLocations);
        bob.getAsyncRemote()
                .sendObject(bobLocations);

        Thread.sleep(10000); // sleep 10 seconds to ensure JWT is expired

        var newLocationAlice = new Location(95, 65, 1000, 0, true, Instant.now());
        var newUserLocationAlice = new UserLocation(userAlice, newLocationAlice);
        var newAliceLocations = new Locations(Set.of(newUserLocationAlice));

        var newLocationBob = new Location(-500, 15, 300, 1, true, Instant.now());
        var newUserLocationBob = new UserLocation(userBob, newLocationBob);
        var newBobLocations = new Locations(Set.of(newUserLocationBob));

        alice.getAsyncRemote()
                .sendObject(newAliceLocations);
        bob.getAsyncRemote()
                .sendObject(newBobLocations);

        Thread.sleep(2000); // wait for relay to be situated

        var allAliceLocations = ALICE_RECEIVED.pollLast();
        var allBobLocations = BOB_RECEIVED.pollLast();

        assertThat(allAliceLocations)
                .isNotNull();
        assertThat(allAliceLocations.data())
                .isNotNull()
                .extracting(UserLocation::user)
                .containsExactlyInAnyOrder(userBob, userAlice);
        assertThat(allAliceLocations.data())
                .isNotNull()
                .extracting(UserLocation::location)
                .containsExactlyInAnyOrder(newLocationBob, newLocationAlice);
        assertThat(allBobLocations)
                .isNotNull();
        assertThat(allBobLocations.data())
                .isNotNull()
                .extracting(UserLocation::user)
                .containsExactly(userBob);
        assertThat(allBobLocations.data())
                .isNotNull()
                .extracting(UserLocation::location)
                .containsExactly(newLocationBob);
    }

    @AfterEach
    public void tearDown() throws Exception {
        alice.close();
        bob.close();
        ALICE_RECEIVED.clear();
        BOB_RECEIVED.clear();
    }
}
