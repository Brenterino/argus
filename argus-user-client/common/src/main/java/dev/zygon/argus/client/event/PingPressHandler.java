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
package dev.zygon.argus.client.event;

import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.location.LocationStorage;
import dev.zygon.argus.client.util.DimensionMapper;
import dev.zygon.argus.client.util.Raycaster;
import dev.zygon.argus.location.Coordinate;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.time.Instant;

@Slf4j
public enum PingPressHandler {

    INSTANCE;

    public void ping() {
        var config = ArgusClientConfig.getActiveConfig();
        if (config.isReadLocalEnvironmentEnabled()) {
            var raycast = Raycaster.raycast();
            if (raycast.getType() == HitResult.Type.ENTITY) {
                hitEntity(raycast);
            } else if (raycast.getType() == HitResult.Type.BLOCK) {
                hitBlock(raycast);
            }
        }
    }

    private void hitEntity(HitResult hit) {
        var entityTrace = (EntityHitResult) hit;
        var entity = entityTrace.getEntity();
        var minecraft = MinecraftClient.getInstance();
        var session = minecraft.getSession();
        var profile = session.getProfile();
        var uuid = profile.getId();
        var dimension = DimensionMapper.currentDimension();
        var position = entity.getPos();
        var location = new Coordinate(position.getX(), position.getY(), position.getZ(),
                dimension.ordinal(), false, Instant.now());
        if (entity instanceof PlayerEntity player) {
            LocationStorage.INSTANCE.trackTarget(player.getUuid(), location);
        } else {
            LocationStorage.INSTANCE.trackPing(uuid, location);
        }
    }

    private void hitBlock(HitResult hit) {
        var minecraft = MinecraftClient.getInstance();
        var session = minecraft.getSession();
        var profile = session.getProfile();
        var playerId = profile.getId();
        var blockTrace = (BlockHitResult) hit;
        var blockPosition = blockTrace.getBlockPos();
        var dimension = DimensionMapper.currentDimension();
        var location = new Coordinate(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(),
                dimension.ordinal(), false, Instant.now());
        LocationStorage.INSTANCE.trackPing(playerId, location);
    }
}
