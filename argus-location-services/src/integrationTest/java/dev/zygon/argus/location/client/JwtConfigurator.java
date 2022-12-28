package dev.zygon.argus.location.client;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.permission.Permission;
import dev.zygon.argus.permission.Permissions;
import io.smallrye.jwt.build.Jwt;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.ClientEndpointConfig;
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
