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
package dev.zygon.argus.client.names;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.Optional;
import java.util.UUID;

public enum NameStorage {

    INSTANCE;

    public String nameFromId(UUID id) {
        // TODO check for custom name first
        return Optional.of(MinecraftClient.getInstance())
                .map(MinecraftClient::getNetworkHandler)
                .map(handler -> handler.getPlayerListEntry(id))
                .map(PlayerListEntry::getProfile)
                .map(GameProfile::getName)
                .orElse(null);
    }

    public UUID idFromName(String name) {
        // TODO backup check from remote
        return Optional.of(MinecraftClient.getInstance())
                .map(MinecraftClient::getNetworkHandler)
                .map(handler -> handler.getPlayerListEntry(name))
                .map(PlayerListEntry::getProfile)
                .map(GameProfile::getId)
                .orElse(null);
    }

    public UUID extendedIdFromName(String name) {
        // TODO need to retrieve extended ID cache from names service
        return UUID.randomUUID();
    }
}
