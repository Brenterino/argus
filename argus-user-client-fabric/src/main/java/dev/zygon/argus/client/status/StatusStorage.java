package dev.zygon.argus.client.status;

import dev.zygon.argus.client.ArgusClient;
import dev.zygon.argus.status.UserStatus;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public enum StatusStorage {

    INSTANCE;

    // TODO not sure if there's a really clean way to clean up data, maybe we can use a cache and evict?

    @Getter
    private final Map<UUID, UserStatus> storage;

    StatusStorage() {
        storage = new ConcurrentHashMap<>();
    }

    public void fromRemote(UserStatus status) {
        storage.put(status.source(), status);
    }

    public void syncRemote(ArgusClient client) {
        var status = UserStatusChecker.INSTANCE.getUserStatus();
        if (status != null) {
            client.getStatuses().send(status);
        }
    }
}
