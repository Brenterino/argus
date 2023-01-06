package dev.zygon.argus.permission;

import lombok.NonNull;

import java.util.List;

public record UserPermissions(@NonNull List<UserPermission> permissions, int pages) {
}
