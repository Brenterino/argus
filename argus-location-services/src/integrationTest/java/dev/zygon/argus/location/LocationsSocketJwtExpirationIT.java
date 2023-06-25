package dev.zygon.argus.location;

import dev.zygon.argus.location.client.LocationsClients.AliceClient;
import dev.zygon.argus.location.client.LocationsClients.BobClient;
import dev.zygon.argus.location.client.LocationsClients.ExpiredTokenClient;
import dev.zygon.argus.location.profile.IntegrationProfile;
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

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
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
        var aliceUUID = UUID.fromString("3b8274a9-aa64-47bc-89a7-64cf896aae93");
        var bobUUID = UUID.fromString("b3c53ad9-cbd9-4644-8371-c2468d8a0e57");
        var userAlice = new User(aliceUUID, "Alice");
        var userBob = new User(bobUUID, "Bob");

        var locationAlice = new Coordinate(101, 50, 2100, 0, true, Instant.now());
        var userLocationAlice = new Location(userAlice, LocationType.USER, locationAlice);
        var aliceLocations = new Locations(Set.of(userLocationAlice));

        var locationBob = new Coordinate(-100, 0, -100, 0, true, Instant.now());
        var userLocationBob = new Location(userBob, LocationType.USER, locationBob);
        var bobLocations = new Locations(Set.of(userLocationBob));

        alice.getAsyncRemote()
                .sendObject(aliceLocations);
        bob.getAsyncRemote()
                .sendObject(bobLocations);

        Thread.sleep(10000); // sleep 10 seconds to ensure JWT is expired

        var newLocationAlice = new Coordinate(95, 65, 1000, 0, true, Instant.now());
        var newUserLocationAlice = new Location(userAlice, LocationType.USER, newLocationAlice);
        var newAliceLocations = new Locations(Set.of(newUserLocationAlice));

        var newLocationBob = new Coordinate(-500, 15, 300, 1, true, Instant.now());
        var newUserLocationBob = new Location(userBob, LocationType.USER, newLocationBob);
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
                .extracting(Location::user)
                .containsExactlyInAnyOrder(userBob, userAlice);
        assertThat(allAliceLocations.data())
                .isNotNull()
                .extracting(Location::coordinates)
                .containsExactlyInAnyOrder(newLocationBob, newLocationAlice);
        assertThat(allBobLocations)
                .isNotNull();
        assertThat(allBobLocations.data())
                .isNotNull()
                .extracting(Location::user)
                .containsExactly(userBob);
        assertThat(allBobLocations.data())
                .isNotNull()
                .extracting(Location::coordinates)
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
