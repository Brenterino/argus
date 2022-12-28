package dev.zygon.argus.permission;

import java.util.Set;

public record UserPermissions(Set<UserPermission> permissions) {
}
