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
import jakarta.websocket.DecodeException;
import jakarta.websocket.EndpointConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class UserStatusDecoderTest {

    private UserStatusDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new UserStatusDecoder();
    }

    @Test
    void decodingBadTextThrowsException() {
        assertThrows(DecodeException.class, () -> decoder.decode(""));
        assertThrows(DecodeException.class, () -> decoder.decode("[]"));
        assertThrows(DecodeException.class, () -> decoder.decode("{}"));
    }

    @Test
    void cannotDecodeLocationDataWithMissingSource() {
        var json =
                """
                        {
                            "items": [],
                            "effects": []
                        }
                        """;

        assertThrows(DecodeException.class, () -> decoder.decode(json));
    }

    @Test
    void cannotDecodeLocationDataWithMissingItems() {
        var json =
                """
                        {
                            "source": "5fc03087-d265-11e7-b8c6-83e29cd24f4c",
                            "effects": []
                        }
                        """;

        assertThrows(DecodeException.class, () -> decoder.decode(json));
    }

    @Test
    void cannotDecodeLocationDataWithMissingEffects() {
        var json =
                """
                        {
                            "source": "5fc03087-d265-11e7-b8c6-83e29cd24f4c",
                            "items": []
                        }
                        """;

        assertThrows(DecodeException.class, () -> decoder.decode(json));
    }

    @Test
    void canDecodeValidUserStatusData() throws DecodeException {
        var json =
                """
                        {
                            "source": "5fc03087-d265-11e7-b8c6-83e29cd24f4c",
                            "items": [
                                {
                                    "color": 0,
                                    "symbol": "L",
                                    "count": 5
                                }
                            ],
                            "effects": [
                                {
                                    "color": 1,
                                    "symbol": "G",
                                    "expiration": "2022-11-23T18:17:44.323877200Z"
                                }
                            ]
                        }
                        """;

        var status = decoder.decode(json);

        assertThat(status)
                .isNotNull();
        assertThat(status.source())
                .isEqualTo(UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"));
        assertThat(status.items())
                .hasSize(1)
                .first()
                .returns(0, ItemStatus::color)
                .returns("L", ItemStatus::symbol)
                .returns(5, ItemStatus::count);
        assertThat(status.effects())
                .hasSize(1)
                .first()
                .returns(1, EffectStatus::color)
                .returns("G", EffectStatus::symbol)
                .returns(Instant.parse("2022-11-23T18:17:44.323877200Z"), EffectStatus::expiration);
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
