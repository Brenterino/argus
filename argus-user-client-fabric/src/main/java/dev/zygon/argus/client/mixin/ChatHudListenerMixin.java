package dev.zygon.argus.client.mixin;

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
        // TODO publish event :)
//        log.info("Hover Text: {}", hoverText);
//        log.info("Message getString(): {}", message.getString());
    }
}
