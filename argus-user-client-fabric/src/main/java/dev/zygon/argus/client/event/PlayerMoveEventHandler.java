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
import dev.zygon.argus.location.Coordinate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.entity.EntityLike;

import java.time.Instant;

public enum PlayerMoveEventHandler {

    INSTANCE;

    public void onEntityMove(EntityLike entity) {
        if (entity.isPlayer()) {
            var minecraft = MinecraftClient.getInstance();
            var session = minecraft.getSession();
            var profile = session.getProfile();
            var playerId = profile.getId();
            var config = ArgusClientConfig.getActiveConfig();
            if (playerId.equals(entity.getUuid()) || config.isReadLocalEntitiesEnabled()) {
                var player = (PlayerEntity) entity;
                var dimension = DimensionMapper.currentDimension();
                var uuid = entity.getUuid();
                var position = player.getPos();
                var location = new Coordinate(position.getX(), position.getY(), position.getZ(),
                        dimension.ordinal(), true, Instant.now());
                LocationStorage.INSTANCE.trackPlayer(uuid, location);
            }
        }
    }
}
