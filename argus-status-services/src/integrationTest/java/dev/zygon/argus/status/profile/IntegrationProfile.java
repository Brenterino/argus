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
package dev.zygon.argus.status.profile;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class IntegrationProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                // Logging
                "quarkus.log.level", "DEBUG",

                // Verify JWT
                "argus.auth.public-key", "public.pem",
                "argus.auth.issuer", "https://argus.zygon.dev/issuer",

                // Sign JWT
                "smallrye.jwt.sign.key.location", "private.pem"
        );
    }
}
