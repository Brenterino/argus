package dev.zygon.argus.permission;

import lombok.NonNull;

import java.util.Set;

public record UserPermissions(@NonNull Set<UserPermission> permissions) {
}
