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

import dev.zygon.argus.status.UserStatus;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

/**
 * Implementation of {@link Encoder.Text} which allows for conversion of
 * a {@link UserStatus} record into JSON using {@link JsonObject}.
 *
 * @see Encoder.Text
 */
public class UserStatusEncoder implements Encoder.Text<UserStatus> {

    @Override
    public String encode(@NonNull UserStatus status) throws EncodeException {
        try {
            return JsonObject.mapFrom(status)
                    .encode();
        } catch (Exception e) {
            throw new EncodeException(status, "Data could not be encoded to JSON", e);
        }
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
