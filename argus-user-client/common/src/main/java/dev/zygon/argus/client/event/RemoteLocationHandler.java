package dev.zygon.argus.client.event;

import dev.zygon.argus.client.ArgusWebSocketClient;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.location.LocationStorage;
import dev.zygon.argus.client.util.DimensionMapper;
import dev.zygon.argus.location.Coordinate;
import dev.zygon.argus.location.Locations;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.MinecraftClient;

import java.time.Instant;

@Slf4j
public enum RemoteLocationHandler {

    INSTANCE;

    private static final String ENDPOINT = "/locations";

    @Setter private ArgusWebSocketClient<Locations> locations;

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
            locations.init(config.getArgusHost(), ENDPOINT);
        }
    }

    public void restartClient() {
        if (!locations.isClosed()) {
            locations.close();
        }
        var config = ArgusClientConfig.getActiveConfig();
        locations.init(config.getArgusHost(), ENDPOINT);
    }

    public void updatePlayerLocation() {
        var minecraft = MinecraftClient.getInstance();
        var player = minecraft.player;
        if (player != null) {
            var dimension = DimensionMapper.currentDimension();
            var position = player.getPos();
            var location = new Coordinate(position.getX(), position.getY(), position.getZ(),
                    dimension.ordinal(), true, Instant.now());
            LocationStorage.INSTANCE.trackPlayer(player.getUuid(), location);
        }
    }
}
