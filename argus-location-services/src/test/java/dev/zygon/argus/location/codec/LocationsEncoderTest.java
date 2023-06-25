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
import dev.zygon.argus.location.LocationType;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.websocket.EncodeException;
import jakarta.websocket.EndpointConfig;
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
        var location = Coordinate.builder()
                .x(100.2)
                .y(200.1)
                .z(300.0)
                .w(1)
                .local(false)
                .time(Instant.parse("2022-11-23T18:17:44.323877200Z"))
                .build();
        var userLocation = new Location(user, LocationType.USER, location);
        var locations = new Locations(Set.of(userLocation));

        var json = encoder.encode(locations);

        assertEquals("{\"data\":[{\"user\":{\"uuid\":\"5fc03087-d265-11e7-b8c6-83e29cd24f4c\",\"name\":\"Zygon\"},\"type\":\"USER\",\"coordinates\":{\"x\":100.2,\"y\":200.1,\"z\":300.0,\"w\":1,\"local\":false,\"time\":\"2022-11-23T18:17:44.323877200Z\"}}]}",
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
