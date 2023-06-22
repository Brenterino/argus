/*
    Argus - Suite of services aimed to enhance Minecraft Multiplayer
    Copyright (C) 2023 Zygon

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.zygon.argus.client.location;

import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.LocationKey;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public enum LocationRenderPreparer {

    INSTANCE;

    @Getter private List<LocationRender> renders = Collections.emptyList();

    public void prepare(Map<LocationKey, Location> storage) {
        var config = ArgusClientConfig.getActiveConfig();
        if (config.isStreamerModeEnabled()) {
            // TODO clear location data so nothing will be rendered
            renders = Collections.emptyList();
        } else {

        }
        var client = MinecraftClient.getInstance();
        var renderDispatcher = client.getEntityRenderDispatcher();
//        var viewDistance = renderDispatcher.gameOptions.getViewDistance();
        var cameraPosition = renderDispatcher.camera.getBlockPos();

    }
}
