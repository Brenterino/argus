package dev.zygon.argus.auth;

import lombok.NonNull;

public record MojangAuthData(@NonNull String server,
                             @NonNull String username,
                             @NonNull String hash) {
}
