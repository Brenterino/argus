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

import dev.zygon.argus.client.event.PlayerMoveEventHandler;
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
        PlayerMoveEventHandler.INSTANCE.onEntityMove(entity);
    }

    @Mixin(targets = "net.minecraft.client.world.ClientEntityManager$Listener")
    public static class ListenerMixin {

        @Final
        @Shadow
        private EntityLike entity;

        @Inject(at = @At("TAIL"), method = "updateEntityPosition")
        public void updateEntityPosition(CallbackInfo ci) {
            PlayerMoveEventHandler.INSTANCE.onEntityMove(entity);
        }
    }
}
