package dev.zygon.argus.auth;

import lombok.NonNull;

import java.time.Instant;

public record ArgusToken(@NonNull String token,
                         @NonNull Instant expiration) {
}
