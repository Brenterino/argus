package dev.zygon.argus.user;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.permission.Permission;
import dev.zygon.argus.permission.Permissions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PermissionsTest {

    @Test
    void permissionsCanBeNull() {
        var permissions = new Permissions(null);

        assertTrue(permissions.permissions().isEmpty());
    }

    @Test
    void permissionsCannotBeMutated() {
        var group = new Group("Foobar");
        var permissions = new Permissions(Map.of(group, Permission.READ));

        var permissionsMap = permissions.permissions();

        assertThrows(UnsupportedOperationException.class, () ->
                permissionsMap.put(group, Permission.WRITE));
    }

    @Test
    void permissionGroupCannotBeDuplicate() {
        var group = new Group("Foobar");
        var permissionsMap = new HashMap<Group, Permission>();
        permissionsMap.put(group, Permission.WRITE);
        permissionsMap.put(group, Permission.READ);
        var permissions = new Permissions(permissionsMap);
        var readGroups = permissions.readGroups();
        var writeGroups = permissions.writeGroups();
        assertFalse(writeGroups.contains(group));
        assertTrue(readGroups.contains(group));
    }

    @Test
    void emptyPermissionsHasNoGroups() {
        var permissions = new Permissions(Collections.emptyMap());

        assertTrue(permissions.readGroups().isEmpty());
        assertTrue(permissions.writeGroups().isEmpty());
        assertTrue(permissions.adminGroups().isEmpty());
    }

    @Test
    void noPermissionsToRawReturnsEmptySet() {
        var permissions = new Permissions(Collections.emptyMap());
        var raw = permissions.toRaw();

        assertTrue(raw.isEmpty());
    }

    @Test
    void permissionsToRawMapsCorrectly() {
        var permissionsMap = Map.of(
                new Group("Estalia"), Permission.READ,
                new Group("Kallos"), Permission.WRITE,
                new Group("Butternut"), Permission.READWRITE,
                new Group("MemeTeam"), Permission.ADMIN
        );
        var permissions = new Permissions(permissionsMap);
        var raw = permissions.toRaw();

        assertTrue(raw.contains("DEFAULT-Estalia-READ"));
        assertTrue(raw.contains("DEFAULT-Kallos-WRITE"));
        assertTrue(raw.contains("DEFAULT-Butternut-READWRITE"));
        assertTrue(raw.contains("DEFAULT-MemeTeam-ADMIN"));
    }

    @Test
    void permissionsFromRawMapsCorrectly() {
        var rawPermissions = Set.of(
                "DEFAULT-Butternut-READ",
                "DEFAULT-Kallos-WRITE",
                "DEFAULT-DC-READWRITE",
                "DEFAULT-Estalia-ADMIN"
        );
        var permissions = Permissions.fromRaw(rawPermissions);
        var write = permissions.writeGroups();
        var read = permissions.readGroups();
        var admin = permissions.adminGroups();

        assertTrue(write.contains(new Group("Kallos")));
        assertTrue(write.contains(new Group("DC")));
        assertTrue(write.contains(new Group("Estalia")));

        assertTrue(read.contains(new Group("Butternut")));
        assertTrue(read.contains(new Group("DC")));
        assertTrue(read.contains(new Group("Estalia")));

        assertTrue(admin.contains(new Group("Estalia")));
    }
}
