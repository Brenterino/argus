package dev.zygon.argus.auth;

import lombok.NonNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record MojangAuthStatus(@NonNull String id,
                               @NonNull String name,
                               @NonNull List<Map<String, String>> properties) {

    public UUID uuid() {
        var numeric = new BigInteger(id, 16);
        return new UUID(numeric.shiftRight(Long.SIZE).longValue(),
                numeric.longValue());
    }
}
