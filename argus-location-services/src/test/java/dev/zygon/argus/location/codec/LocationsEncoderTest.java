package dev.zygon.argus.location.codec;

import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.UserLocation;
import dev.zygon.argus.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class LocationsEncoderTest {

    private LocationsEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new LocationsEncoder();
    }

    @Test
    void encodingNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                encoder.encode(null));
    }

    @Test
    void canEncodeEmptyLocations() throws EncodeException {
        var locations = new Locations(Collections.emptySet());

        var json = encoder.encode(locations);

        assertEquals("{\"data\":[]}", json);
    }

    @Test
    void canEncodeSingleLocation() throws EncodeException {
        var uuid = UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c");
        var user = new User(uuid, "Zygon");
        var location = Location.builder()
                .x(100.2)
                .y(200.1)
                .z(300.0)
                .w(1)
                .local(false)
                .time(Instant.parse("2022-11-23T18:17:44.323877200Z"))
                .build();
        var userLocation = new UserLocation(user, location);
        var locations = new Locations(Set.of(userLocation));

        var json = encoder.encode(locations);

        assertEquals("{\"data\":[{\"user\":{\"uuid\":\"5fc03087-d265-11e7-b8c6-83e29cd24f4c\",\"name\":\"Zygon\"},\"location\":{\"x\":100.2,\"y\":200.1,\"z\":300.0,\"w\":1,\"local\":false,\"time\":\"2022-11-23T18:17:44.323877200Z\"}}]}",
                json);
    }

    @Test
    void initHasNoSideEffects() {
        var config = mock(EndpointConfig.class);

        encoder.init(config);

        verifyNoInteractions(config);
    }

    @Test
    void destroyHasNoSideEffects() {
        encoder.destroy();
    }
}
