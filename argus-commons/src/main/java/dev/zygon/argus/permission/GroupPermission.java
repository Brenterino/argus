package dev.zygon.argus.permission;

import dev.zygon.argus.group.Group;
import lombok.NonNull;

public record GroupPermission(@NonNull Group group, @NonNull Permission permission) {
}
