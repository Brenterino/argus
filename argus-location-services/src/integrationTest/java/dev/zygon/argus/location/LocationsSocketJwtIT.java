package dev.zygon.argus.location;

import dev.zygon.argus.location.client.AliceJwtConfigurator;
import dev.zygon.argus.location.client.BobJwtConfigurator;
import dev.zygon.argus.location.codec.LocationsDecoder;
import dev.zygon.argus.location.codec.LocationsEncoder;
import dev.zygon.argus.user.User;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.websocket.*;
import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@QuarkusTest
@TestProfile(IntegrationProfile.class)
public class LocationsSocketJwtIT {

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
        var aliceToAlice = ALICE_RECEIVED.poll(2, TimeUnit.SECONDS);
        var aliceToBob = BOB_RECEIVED.poll(2, TimeUnit.SECONDS);
        bob.getAsyncRemote()
                .sendObject(bobLocations);
        var bobToAlice = ALICE_RECEIVED.poll(2, TimeUnit.SECONDS);
        var bobToBob = BOB_RECEIVED.poll(2, TimeUnit.SECONDS);

        // Alice sent a message, only Alice receives it
        assertThat(aliceToAlice)
                .isNotNull();
        assertThat(aliceToAlice.data())
                .isNotNull()
                .extracting(UserLocation::user)
                .containsExactly(userAlice);
        assertThat(aliceToAlice.data())
                .isNotNull()
                .extracting(UserLocation::location)
                .containsExactly(locationAlice);
        assertThat(aliceToBob)
                .isNull();

        // Bob sent a message, both parties receive it
        assertThat(bobToAlice)
                .isNotNull();
        assertThat(bobToAlice.data())
                .isNotNull()
                .extracting(UserLocation::user)
                .containsExactly(userBob);
        assertThat(bobToAlice.data())
                .isNotNull()
                .extracting(UserLocation::location)
                .containsExactly(locationBob);
        assertThat(bobToBob)
                .isNotNull();
        assertThat(bobToBob.data())
                .isNotNull()
                .extracting(UserLocation::user)
                .containsExactly(userBob);
        assertThat(bobToBob.data())
                .isNotNull()
                .extracting(UserLocation::location)
                .containsExactly(locationBob);
    }

    @AfterEach
    public void tearDown() throws Exception {
        alice.close();
        bob.close();
        ALICE_RECEIVED.clear();
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
