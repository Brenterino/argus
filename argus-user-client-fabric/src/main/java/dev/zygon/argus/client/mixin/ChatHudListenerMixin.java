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

import dev.zygon.argus.client.event.ChatEventManager;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.network.MessageType;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;

@Mixin(ChatHudListener.class)
public abstract class ChatHudListenerMixin {

    @Inject(at = @At("HEAD"), method = "onChatMessage", cancellable = true)
    public void onChatMessage(MessageType type, Text message, UUID sender, CallbackInfo ci) {
        var text = message.getString();
        var hoverText = message.getSiblings()
                .stream()
                .map(Text::getStyle)
                .map(Style::getHoverEvent)
                .filter(Objects::nonNull)
                .map(event -> event.getValue(HoverEvent.Action.SHOW_TEXT))
                .filter(Objects::nonNull)
                .findFirst()
                .map(Text::getString)
                .orElse(null);
        var shouldCancel = ChatEventManager.INSTANCE.onChatMessage(text, hoverText);
        if (shouldCancel) {
            ci.cancel();
        }
    }
}
