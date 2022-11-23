package dev.zygon.argus.location.codec;

import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.UserLocation;
import dev.zygon.argus.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.websocket.DecodeException;
import javax.websocket.EndpointConfig;
import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.DOUBLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class LocationsDecoderTest {

    private LocationsDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new LocationsDecoder();
    }

    @Test
    void decodingBadTextThrowsException() {
        assertThrows(DecodeException.class, () -> decoder.decode(""));
        assertThrows(DecodeException.class, () -> decoder.decode("[]"));
    }

    @Test
    void canDecodeEmptyLocationData() throws DecodeException {
        var json = "{}";

        var locations = decoder.decode(json);
        var data = locations.data();

        assertThat(data)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void cannotDecodeLocationDataWithMissingUserNode() {
        var json =
                """
                        {
                            "data": [
                                {
                                    "location": {
                                        "x": 100.25,
                                        "y": -200.50,
                                        "z": 1080.75,
                                        "w": 0,
                                        "local": false,
                                        "time": "2022-11-23T18:17:44.323877200Z"
                                    }
                                }
                            ]
                        }
                        """;

        assertThrows(DecodeException.class, () -> decoder.decode(json));
    }

    @Test
    void cannotDecodeLocationDataWithMissingLocationNode() {
        var json =
                """
                        {
                            "data": [
                                {
                                    "user": {
                                        "uuid": "1",
                                        "name": "Walkers"
                                    }
                                }
                            ]
                        }
                        """;

        assertThrows(DecodeException.class, () -> decoder.decode(json));
    }

    @Test
    void canDecodeLocationDataWithOneLocation() throws DecodeException {
        var json =
                """
                        {
                            "data": [
                                {
                                    "user": {
                                        "uuid": "1",
                                        "name": "Walkers"
                                    },
                                    "location": {
                                        "x": 100.25,
                                        "y": -200.50,
                                        "z": 1080.75,
                                        "w": 0,
                                        "local": false,
                                        "time": "2022-11-23T18:17:44.323877200Z"
                                    }
                                }
                            ]
                        }
                        """;

        var locations = decoder.decode(json);

        System.out.println(locations);

        assertThat(locations)
                .isNotNull();
        assertThat(locations.data())
                .extracting(UserLocation::user)
                .singleElement()
                .isNotNull()
                .hasFieldOrPropertyWithValue("uuid", "1")
                .hasFieldOrPropertyWithValue("name", "Walkers")
                .extracting(User::metadata)
                .isEqualTo(Collections.emptyMap());
        assertThat(locations.data())
                .extracting(UserLocation::location)
                .singleElement()
                .isNotNull()
                .extracting(Location::x, as(DOUBLE))
                .isCloseTo(100.25, offset(0.01));
        assertThat(locations.data())
                .extracting(UserLocation::location)
                .singleElement()
                .isNotNull()
                .extracting(Location::y, as(DOUBLE))
                .isCloseTo(-200.50, offset(0.01));
        assertThat(locations.data())
                .extracting(UserLocation::location)
                .singleElement()
                .isNotNull()
                .extracting(Location::z, as(DOUBLE))
                .isCloseTo(1080.75, offset(0.01));
        assertThat(locations.data())
                .extracting(UserLocation::location)
                .singleElement()
                .isNotNull()
                .hasFieldOrPropertyWithValue("w", 0)
                .hasFieldOrPropertyWithValue("local", false)
                .hasFieldOrPropertyWithValue("time", Instant.parse("2022-11-23T18:17:44.323877200Z"));
    }

    @Test
    void willNotDecodeTextThatIsNullOrBlank() {
        assertFalse(decoder.willDecode(null));
        assertFalse(decoder.willDecode(""));
        assertFalse(decoder.willDecode("  "));
    }

    @Test
    void willDecodeTextThatIsNotEmpty() {
        assertTrue(decoder.willDecode("a"));
        assertTrue(decoder.willDecode("{}"));
        assertTrue(decoder.willDecode("[]"));
    }

    @Test
    void initHasNoSideEffects() {
        var config = mock(EndpointConfig.class);

        decoder.init(config);

        verifyNoInteractions(config);
    }

    @Test
    void destroyHasNoSideEffects() {
        decoder.destroy();
    }
}
