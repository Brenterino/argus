package dev.zygon.argus.client.mixin;

import dev.zygon.argus.client.config.ArgusClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientEntityManager;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientEntityManager.class)
public abstract class ClientEntityManagerMixin {

    @Inject(at = @At("TAIL"), method = "addEntity")
    public void addEntity(EntityLike entity, CallbackInfo ci) {
        if (entity.isPlayer()) {
            var minecraft = MinecraftClient.getInstance();
            var session = minecraft.getSession();
            var profile = session.getProfile();
            var playerId = profile.getId();
            var config = ArgusClientConfig.getActiveConfig();
            if (playerId.equals(entity.getUuid()) || config.isReadLocalEntitiesEnabled()) {
            // TODO publish event
//            log.info("Adding Player Entity - UUID = {}, Block Pos = {}",
//                    entity.getUuid(), entity.getBlockPos());
//            var minecraft = MinecraftClient.getInstance();
//
//            var world = Optional.of(minecraft)
//                    .map(client -> client.player)
//                    .map(Entity::getEntityWorld)
//                    .map(World::getRegistryKey)
//                    .map(RegistryKey::getValue)
//                    .map(Identifier::getPath)
//                    .map(DimensionMapper::fromProximity)
//                    .orElse(Dimension.OVERWORLD);
            }
        }
    }

    @Mixin(targets = "net.minecraft.client.world.ClientEntityManager$Listener")
    public static class ListenerMixin {

        @Final
        @Shadow
        private EntityLike entity;

        @Inject(at = @At("TAIL"), method = "updateEntityPosition")
        public void updateEntityPosition(CallbackInfo ci) {
            var config = ArgusClientConfig.getActiveConfig();
            if (config.isReadLocalEntitiesEnabled() && entity.isPlayer()) {
//                log.info("Updating Player Entity - UUID = {}, Block Pos = {}",
//                        entity.getUuid(), entity.getBlockPos());
            }
        }
    }
}
