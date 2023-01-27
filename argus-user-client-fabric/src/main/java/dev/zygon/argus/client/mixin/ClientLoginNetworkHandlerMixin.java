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
