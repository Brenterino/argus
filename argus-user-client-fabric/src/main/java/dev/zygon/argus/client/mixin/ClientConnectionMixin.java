package dev.zygon.argus.client.mixin;

import dev.zygon.argus.client.connector.ArgusClientConnector;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Slf4j
@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Inject(at = @At("TAIL"), method = "channelInactive")
    public void channelInactive(ChannelHandlerContext context, CallbackInfo ci) {
        log.info("[ARGUS] Connection was closed.");
        try {
            ArgusClientConnector.INSTANCE.close();
        } catch (Throwable t) {
            log.error("[ARGUS] Encountered exception while closing Argus client.", t);
        }
    }
}
