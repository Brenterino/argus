package dev.zygon.argus.auth.service;

import dev.zygon.argus.auth.ArgusToken;
import dev.zygon.argus.auth.configuration.AuthConfiguration;
import dev.zygon.argus.permission.Permissions;
import io.smallrye.jwt.build.Jwt;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@ApplicationScoped
public class ArgusJwtTokenGenerator implements ArgusTokenGenerator {

    private static final String UPN_SPLIT = "@";

    private final AuthConfiguration configuration;

    public ArgusJwtTokenGenerator(AuthConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ArgusToken generate(UUID uuid, String namespace, Permissions permissions) {
        var expiration = Instant.now()
                .plus(Duration.of(configuration.tokenExpirationMinutes(), ChronoUnit.MINUTES));
        var token = Jwt.issuer(configuration.issuer())
                .upn(uuid.toString() + UPN_SPLIT + namespace)
                .groups(permissions.toRaw())
                .expiresAt(expiration)
                .sign();
        return new ArgusToken(token, expiration);
    }
}
