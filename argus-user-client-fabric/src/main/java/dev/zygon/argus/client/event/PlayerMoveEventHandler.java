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
import dev.zygon.argus.client.location.LocalLocationStorage;
import dev.zygon.argus.client.util.DimensionMapper;
import dev.zygon.argus.location.Dimension;
import dev.zygon.argus.location.Coordinate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;

import java.time.Instant;
import java.util.Optional;

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
                var dimension = Optional.of(minecraft)
                        .map(client -> client.player)
                        .map(Entity::getEntityWorld)
                        .map(World::getRegistryKey)
                        .map(RegistryKey::getValue)
                        .map(Identifier::getPath)
                        .map(DimensionMapper::fromProximity)
                        .orElse(Dimension.OVERWORLD);
                var uuid = entity.getUuid();
                var position = entity.getBlockPos();
                var location = new Coordinate(position.getX(), position.getY(), position.getZ(),
                        dimension.ordinal(), true, Instant.now());
                LocalLocationStorage.INSTANCE.track(uuid, location);
            }
        }
    }
}
