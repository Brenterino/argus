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
package dev.zygon.argus.client.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.zygon.argus.auth.ArgusToken;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import retrofit2.converter.jackson.JacksonConverterFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JacksonUtil {

    public static JacksonConverterFactory converterFactory() {
        return JacksonConverterFactory.create(customObjectMapper());
    }

    public static ObjectMapper customObjectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    public static ObjectMapper jsr310ObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final ObjectMapper TOKEN_MAPPER =
            jsr310ObjectMapper();

    public static String stringfyToken(ArgusToken token) {
        try {
            return TOKEN_MAPPER.writeValueAsString(token);
        } catch (Exception e) { // swallow? :)
            return "{}";
        }
    }
}
