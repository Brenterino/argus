package dev.zygon.argus.client.location;

import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.LocationKey;
import net.minecraft.client.MinecraftClient;

import java.util.Map;

public enum LocationRenderPreparer {

    INSTANCE;

    public void prepare(Map<LocationKey, Location> storage) {
        var config = ArgusClientConfig.getActiveConfig();
        if (config.isStreamerModeEnabled()) {
            // TODO clear location data so nothing will be rendered
        } else {

        }
        var client = MinecraftClient.getInstance();
        var renderDispatcher = client.getEntityRenderDispatcher();
//        var viewDistance = renderDispatcher.gameOptions.getViewDistance();
        var cameraPosition = renderDispatcher.camera.getBlockPos();

    }
}
