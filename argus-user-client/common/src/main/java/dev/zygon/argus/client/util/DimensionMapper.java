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
package dev.zygon.argus.client.util;

import dev.zygon.argus.location.Dimension;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DimensionMapper {

    public static Dimension fromSnitch(String dimension) {
        return switch (dimension) {
            case "world_nether" -> Dimension.NETHER;
            case "world_the_end" -> Dimension.END;
            default -> Dimension.OVERWORLD;
        };
    }

    public static Dimension fromProximity(String dimension) {
        return switch (dimension) {
            case "the_nether" -> Dimension.NETHER;
            case "the_end" -> Dimension.END;
            default -> Dimension.OVERWORLD;
        };
    }

    public static Dimension currentDimension() {
        var minecraft = MinecraftClient.getInstance();
        return Optional.of(minecraft)
                .map(client -> client.player)
                .map(Entity::getEntityWorld)
                .map(World::getRegistryKey)
                .map(RegistryKey::getValue)
                .map(Identifier::getPath)
                .map(DimensionMapper::fromProximity)
                .orElse(Dimension.OVERWORLD);
    }
}
