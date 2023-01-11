package dev.zygon.argus.client.mixin;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.world.ClientEntityManager;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Slf4j(topic = "Argus-ClientEntityManagerMixin")
@Mixin(ClientEntityManager.class)
public class ClientEntityManagerMixin {

    @Inject(at = @At("TAIL"), method = "addEntity")
    public void addEntity(EntityLike entity, CallbackInfo ci) {
        if (entity.isPlayer()) {
            // TODO publish event
//            log.info("Adding Player Entity - UUID = {}, Block Pos = {}",
//                    entity.getUuid(), entity.getBlockPos());
        }
    }

    @Slf4j(topic = "Argus-ClientEntityManagerListenerMixin")
    @Mixin(targets = "net.minecraft.client.world.ClientEntityManager$Listener")
    public static class ListenerMixin {

        @Shadow
        private EntityLike entity;

        @Inject(at = @At("TAIL"), method = "updateEntityPosition")
        public void updateEntityPosition(CallbackInfo ci) {
            if (entity.isPlayer()) {
                // TODO publish event
//                log.info("Updating Player Entity - UUID = {}, Block Pos = {}",
//                        entity.getUuid(), entity.getBlockPos());
            }
        }
    }
}
