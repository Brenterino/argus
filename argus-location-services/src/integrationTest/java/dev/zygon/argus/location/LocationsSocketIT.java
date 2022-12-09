package dev.zygon.argus.location;

import dev.zygon.argus.location.client.AliceJwtConfigurator;
import dev.zygon.argus.location.client.BobJwtConfigurator;
import dev.zygon.argus.location.codec.LocationsDecoder;
import dev.zygon.argus.location.codec.LocationsEncoder;
import dev.zygon.argus.user.User;
import io.quarkus.test.common.http.TestHTTPResource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.websocket.*;
import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Disabled("Only meant to be used as a base test and should not run directly.")
public class LocationsSocketIT {

    private static final LinkedBlockingDeque<Locations> ALICE_RECEIVED =
            new LinkedBlockingDeque<>();
    private static final LinkedBlockingDeque<Locations> BOB_RECEIVED =
            new LinkedBlockingDeque<>();
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
    public void testLocationMessage() throws Exception {
        var userAlice = new User("1", "Alice");
        var userBob = new User("2", "Bob");
        var locationAlice = new Location(101, 50, 2100, 0, true, Instant.now());
        var locationBob = new Location(-100, 0, -100, 0, true, Instant.now());
        var userLocationAlice = new UserLocation(userAlice, locationAlice);
        var userLocationBob = new UserLocation(userBob, locationBob);
        var aliceLocations = new Locations(Set.of(userLocationAlice));
        var bobLocations = new Locations(Set.of(userLocationBob));

        alice.getAsyncRemote()
                .sendObject(aliceLocations);
        bob.getAsyncRemote()
                .sendObject(bobLocations);
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
                .containsExactlyInAnyOrder(locationBob, locationAlice);
        assertThat(allBobLocations)
                .isNotNull();
        assertThat(allBobLocations.data())
                .isNotNull()
                .extracting(UserLocation::user)
                .containsExactly(userBob);
        assertThat(allBobLocations.data())
                .isNotNull()
                .extracting(UserLocation::location)
                .containsExactly(locationBob);
    }

    @AfterEach
    public void tearDown() throws Exception {
        alice.close();
        bob.close();
        ALICE_RECEIVED.clear();
        BOB_RECEIVED.clear();
    }

    @ClientEndpoint(encoders = LocationsEncoder.class,
            decoders = LocationsDecoder.class,
            configurator = AliceJwtConfigurator.class)
    public static class AliceClient {

        @OnOpen
        public void onOpen(Session session) {
            log.info("Opened Alice client.");
        }

        @OnMessage
        public void onMessage(Session session, Locations locations) {
            ALICE_RECEIVED.add(locations);
        }
    }

    @ClientEndpoint(encoders = LocationsEncoder.class,
            decoders = LocationsDecoder.class,
            configurator = BobJwtConfigurator.class)
    public static class BobClient {

        @OnOpen
        public void onOpen(Session session) {
            log.info("Opened Bob client.");
        }

        @OnMessage
        public void onMessage(Session session, Locations locations) {
            BOB_RECEIVED.add(locations);
        }
    }
}