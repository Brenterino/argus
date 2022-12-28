package dev.zygon.argus.user;

import lombok.NonNull;

public record NamespaceUser(@NonNull String namespace, @NonNull User user) {
}
