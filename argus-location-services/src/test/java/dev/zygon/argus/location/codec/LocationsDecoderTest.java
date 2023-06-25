/*
    Argus - Suite of services aimed to enhance Minecraft Multiplayer
    Copyright (C) 2023 Zygon

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.zygon.argus.location.codec;

import dev.zygon.argus.location.Coordinate;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.LocationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.websocket.DecodeException;
import jakarta.websocket.EndpointConfig;
import java.time.Instant;
import java.util.UUID;

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
                                        "uuid": "5fc03087-d265-11e7-b8c6-83e29cd24f4c",
                                        "name": "Walkers"
                                    },
                                    "type": "USER",
                                    "coordinates": {
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
                .extracting(Location::user)
                .singleElement()
                .isNotNull()
                .hasFieldOrPropertyWithValue("uuid", UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"))
                .hasFieldOrPropertyWithValue("name", "Walkers");
        assertThat(locations.data())
                .extracting(Location::type)
                .singleElement()
                .isNotNull()
                .isEqualTo(LocationType.USER);
        assertThat(locations.data())
                .extracting(Location::coordinates)
                .singleElement()
                .isNotNull()
                .extracting(Coordinate::x, as(DOUBLE))
                .isCloseTo(100.25, offset(0.01));
        assertThat(locations.data())
                .extracting(Location::coordinates)
                .singleElement()
                .isNotNull()
                .extracting(Coordinate::y, as(DOUBLE))
                .isCloseTo(-200.50, offset(0.01));
        assertThat(locations.data())
                .extracting(Location::coordinates)
                .singleElement()
                .isNotNull()
                .extracting(Coordinate::z, as(DOUBLE))
                .isCloseTo(1080.75, offset(0.01));
        assertThat(locations.data())
                .extracting(Location::coordinates)
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
