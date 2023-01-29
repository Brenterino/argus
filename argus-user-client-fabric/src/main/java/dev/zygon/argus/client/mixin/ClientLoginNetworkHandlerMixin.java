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
package dev.zygon.argus.client.mixin;

import dev.zygon.argus.client.connector.ArgusClientConnector;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Slf4j
@Mixin(ClientLoginNetworkHandler.class)
public abstract class ClientLoginNetworkHandlerMixin {

    @Final
    @Shadow
    private MinecraftClient client;

    @Final
    @Shadow
    private ClientConnection connection;

    @Inject(at = @At("TAIL"), method = "onSuccess")
    public void onSuccess(LoginSuccessS2CPacket packet, CallbackInfo ci) {
        var session = client.getSession();
        var server = connection.getAddress().toString();
        log.info("[ARGUS] Connected to server {}. Starting Argus Client.", server);
        try {
            ArgusClientConnector.INSTANCE
                    .open(server, session.getUsername());
        } catch (Throwable t) {
            log.error("[ARGUS] Encountered exception while opening Argus client.", t);
        }
    }
}
