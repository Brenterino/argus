package dev.zygon.argus.location;

import dev.zygon.argus.location.codec.LocationsDecoder;
import dev.zygon.argus.location.codec.LocationsEncoder;
import dev.zygon.argus.user.User;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.websocket.*;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@QuarkusTest
public class LocationsSocketIT {

    private static final LinkedBlockingDeque<Locations> RECEIVED =
            new LinkedBlockingDeque<>();
    @TestHTTPResource("/locations")
    private URI uri;
    private Session alice;
    private Session bob;

    @BeforeEach
    public void setUp() throws Exception {
        alice = ContainerProvider.getWebSocketContainer()
                .connectToServer(LocationClient.class, uri);
        bob = ContainerProvider.getWebSocketContainer()
                .connectToServer(LocationClient.class, uri);
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
        var echoAlice = RECEIVED.poll(10, TimeUnit.SECONDS);
        bob.getAsyncRemote()
                .sendObject(bobLocations);
        var echoBob = RECEIVED.poll(10, TimeUnit.SECONDS);

        assertThat(echoAlice)
                .isNotNull();
        assertThat(echoAlice.data())
                .isNotNull()
                .extracting(UserLocation::user)
                .containsExactly(userAlice);
        assertThat(echoAlice.data())
                .isNotNull()
                .extracting(UserLocation::location)
                .containsExactly(locationAlice);
        assertThat(echoBob)
                .isNotNull();
        assertThat(echoBob.data())
                .isNotNull()
                .extracting(UserLocation::user)
                .containsExactly(userBob);
        assertThat(echoBob.data())
                .isNotNull()
                .extracting(UserLocation::location)
                .containsExactly(locationBob);
    }

    @AfterEach
    public void tearDown() throws Exception {
        alice.close();
        bob.close();
        RECEIVED.clear();
    }

    @ClientEndpoint(encoders = LocationsEncoder.class,
            decoders = LocationsDecoder.class,
            configurator = CustomConfigurator.class)
    public static class LocationClient {

        @OnOpen
        public void onOpen(Session session) {
            log.info("Opened client session");
        }

        @OnMessage
        public void onMessage(Session session, Locations locations) {
            RECEIVED.add(locations);
        }
    }

    public static class CustomConfigurator extends ClientEndpointConfig.Configurator {

        public void beforeRequest(Map<String, List<String>> headers) {
            // TODO generate JWT wrapping group claims in authorization header
            log.info("Entry before request");
        }
    }
}
