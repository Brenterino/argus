package dev.zygon.argus.location.client;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.permission.Permission;
import dev.zygon.argus.permission.Permissions;

import java.util.Map;

public class BobJwtConfigurator extends JwtConfigurator {

    @Override
    protected Permissions generatePermissions() {
        var alice = new Group("alice");
        var bob = new Group("bob");
        var permissions = Map.of(
                alice, Permission.WRITE,
                bob, Permission.ADMIN
        );
        return new Permissions(permissions);
    }
}
