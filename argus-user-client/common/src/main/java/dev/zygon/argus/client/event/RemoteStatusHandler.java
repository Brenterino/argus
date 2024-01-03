package dev.zygon.argus.client.event;

import dev.zygon.argus.client.ArgusWebSocketClient;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.status.StatusStorage;
import dev.zygon.argus.status.UserStatus;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum RemoteStatusHandler {

    INSTANCE;

    private static final String ENDPOINT = "/statuses";

    @Setter
    private ArgusWebSocketClient<UserStatus> statuses;

    public void onStatusReceived(UserStatus data) {
        StatusStorage.INSTANCE.fromRemote(data);
    }

    public void onRemoteSyncFailure(Throwable cause) {
        log.error("[ARGUS] Remote synchronization failed.", cause);
        // TODO there might be more we can here and it's possible this is the wrong abstraction level
        //      to handle this error anyways. I think maybe the proper one that can actually disposition
        //      this more intelligently is in the client connector level, but leaving here for now to
        //      collect in one place.
    }

    public void keepClientAlive() {
        if (statuses.isClosed()) {
            var config = ArgusClientConfig.getActiveConfig();
            statuses.init(config.getArgusHost(), ENDPOINT);
        }
    }

    public void restartClient() {
        if (!statuses.isClosed()) {
            statuses.close();
        }
        var config = ArgusClientConfig.getActiveConfig();
        statuses.init(config.getArgusHost(), ENDPOINT);
    }
}
