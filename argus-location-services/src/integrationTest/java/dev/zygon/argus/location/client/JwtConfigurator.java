package dev.zygon.argus.location.client;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.Permission;
import dev.zygon.argus.user.Permissions;
import io.smallrye.jwt.build.Jwt;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.ClientEndpointConfig;
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

    public String generateToken(Permissions permissions) {
        return Jwt.issuer("https://argus.zygon.dev/issuer")
                .upn("alice")
                .groups(permissions.toRaw())
                .sign();
    }
}
