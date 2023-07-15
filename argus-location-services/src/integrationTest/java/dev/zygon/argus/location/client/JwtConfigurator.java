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
package dev.zygon.argus.location.client;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.permission.Permission;
import dev.zygon.argus.permission.Permissions;
import io.smallrye.jwt.build.Jwt;
import lombok.extern.slf4j.Slf4j;

import jakarta.websocket.ClientEndpointConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
public class JwtConfigurator extends ClientEndpointConfig.Configurator {

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        var permissions = generatePermissions();
        var token = "Bearer " + generateToken(permissions);
        headers.put("Authorization", List.of(token));

        log.info("Headers attached to request: {}",
                headers);
    }

    protected Permissions generatePermissions() {
        var group = new Group("group");
        var permissions = Map.of(group, Permission.READWRITE);
        return new Permissions(permissions);
    }

    protected String generateToken(Permissions permissions) {
        var expiration = Instant.now(Clock.systemUTC())
                .plus(5, ChronoUnit.SECONDS);
        return generateToken(permissions, expiration);
    }

    protected String generateToken(Permissions permissions, Instant expiration) {
        return Jwt.issuer("https://argus.zygon.dev/issuer")
                .upn("user@test")
                .groups(permissions.toRaw())
                .expiresAt(expiration)
                .sign();
    }
}
