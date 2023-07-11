package dev.zygon.argus.client.event;

import dev.zygon.argus.client.ArgusLocationsClient;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.location.LocationStorage;
import dev.zygon.argus.location.Locations;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum RemoteLocationHandler {

    INSTANCE;

    @Setter private ArgusLocationsClient locations;

    public void onLocationsReceived(Locations data) {
        LocationStorage.INSTANCE.fromRemote(data);
    }

    public void onRemoteSyncFailure(Throwable cause) {
        log.error("[ARGUS] Remote synchronization failed.", cause);
        // TODO there might be more we can here and it's possible this is the wrong abstraction level
        //      to handle this error anyways. I think maybe the proper one that can actually disposition
        //      this more intelligently is in the client connector level, but leaving here for now to
        //      collect in one place.
    }

    public void keepClientAlive() {
        if (locations.isClosed()) {
            var config = ArgusClientConfig.getActiveConfig();
            locations.init(config.getArgusHost());
        }
    }

    public void restartClient() {
        if (!locations.isClosed()) {
            locations.close();
        }
        var config = ArgusClientConfig.getActiveConfig();
        locations.init(config.getArgusHost());
    }
}
