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
