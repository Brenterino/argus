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
package dev.zygon.argus.status.codec;

import dev.zygon.argus.status.EffectStatus;
import dev.zygon.argus.status.ItemStatus;
import dev.zygon.argus.status.UserStatus;
import jakarta.websocket.EncodeException;
import jakarta.websocket.EndpointConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class UserStatusEncoderTest {

    private UserStatusEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new UserStatusEncoder();
    }

    @Test
    void encodingNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                encoder.encode(null));
    }

    @Test
    void canEncodeEmptyStatus() throws EncodeException {
        var status = new UserStatus(UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"),
                20.0f, Collections.emptyList(),
                Collections.emptyList());

        var json = encoder.encode(status);

        assertEquals("{\"source\":\"5fc03087-d265-11e7-b8c6-83e29cd24f4c\",\"health\":20.0,\"items\":[],\"effects\":[]}", json);
    }

    @Test
    void canEncodeSingleLocation() throws EncodeException {
        var uuid = UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c");
        var item = new ItemStatus(0, "L", 5);
        var effect = new EffectStatus(0, "G", Instant.parse("2022-11-23T18:17:44.323877200Z"));
        var status = new UserStatus(uuid, 5.5f, List.of(item), List.of(effect));

        var json = encoder.encode(status);

        assertEquals("{\"source\":\"5fc03087-d265-11e7-b8c6-83e29cd24f4c\",\"health\":5.5,\"items\":[{\"color\":0,\"symbol\":\"L\",\"count\":5}],\"effects\":[{\"color\":0,\"symbol\":\"G\",\"expiration\":\"2022-11-23T18:17:44.323877200Z\"}]}",
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
