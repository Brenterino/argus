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

import dev.zygon.argus.location.Locations;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

/**
 * Implementation of {@link Decoder.Text} which allows for conversion of
 * JSON into a {@link Locations} record using {@link JsonObject}.
 *
 * @see Decoder.Text
 */
@Slf4j
public class LocationsDecoder implements Decoder.Text<Locations> {

    @Override
    public Locations decode(@NonNull String text) throws DecodeException {
        try {
            return new JsonObject(text)
                    .mapTo(Locations.class);
        } catch (Exception e) {
            throw new DecodeException(text, "Locations could not be decoded from JSON", e);
        }
    }

    @Override
    public boolean willDecode(String text) {
        return text != null && !text.isBlank();
    }

    @Override
    public void init(EndpointConfig config) {
        // No initialization required
    }

    @Override
    public void destroy() {
        // No destroy override required
    }
}
