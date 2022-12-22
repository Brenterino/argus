package dev.zygon.argus.location.client;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.Permission;
import dev.zygon.argus.user.Permissions;

import java.util.Map;

public class AliceJwtConfigurator extends JwtConfigurator {

    @Override
    protected Permissions generatePermissions() {
        var alice = new Group("alice");
        var permissions = Map.of(alice, Permission.ADMIN);
        return new Permissions(permissions);
    }
}
