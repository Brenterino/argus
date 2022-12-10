package dev.zygon.argus.location.client;

import dev.zygon.argus.user.Permissions;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ExpiredJwtConfigurator extends JwtConfigurator {

    @Override
    protected String generateToken(Permissions permissions) {
        var expiration = Instant.now(Clock.systemUTC())
                .minus(30, ChronoUnit.SECONDS);
        return generateToken(permissions, expiration);
    }
}
