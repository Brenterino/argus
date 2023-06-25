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
package dev.zygon.argus.auth.service.impl;

import dev.zygon.argus.auth.ArgusToken;
import dev.zygon.argus.auth.configuration.AuthConfiguration;
import dev.zygon.argus.auth.service.ArgusTokenGenerator;
import dev.zygon.argus.permission.Permissions;
import io.smallrye.jwt.build.Jwt;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

@ApplicationScoped
public class ArgusJwtTokenGenerator implements ArgusTokenGenerator {

    private static final String UPN_SPLIT = "@";

    private final AuthConfiguration configuration;

    public ArgusJwtTokenGenerator(AuthConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ArgusToken generateRefreshToken(UUID uuid, String namespace) {
        var permissions = new Permissions(Collections.emptyMap());
        var expiration = Instant.now(Clock.systemUTC())
                .plus(Duration.of(configuration.refreshTokenExpirationMinutes(), ChronoUnit.MINUTES));
        return generateToken(uuid, namespace, permissions, expiration);
    }

    @Override
    public ArgusToken generateAccessToken(UUID uuid, String namespace, Permissions permissions) {
        var expiration = Instant.now(Clock.systemUTC())
                .plus(Duration.of(configuration.accessTokenExpirationMinutes(), ChronoUnit.MINUTES));
        return generateToken(uuid, namespace, permissions, expiration);
    }

    private ArgusToken generateToken(UUID uuid, String namespace, Permissions permissions, Instant expiration) {
        var token = Jwt.issuer(configuration.issuer())
                .upn(uuid.toString() + UPN_SPLIT + namespace)
                .groups(permissions.toRaw())
                .expiresAt(expiration)
                .sign();
        return new ArgusToken(token, expiration);
    }
}
