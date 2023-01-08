package dev.zygon.argus.client.domain;

import lombok.NonNull;

public record ServerJoinData(@NonNull String accessToken,
                             @NonNull String selectedProfile,
                             @NonNull String serverId) {
}
