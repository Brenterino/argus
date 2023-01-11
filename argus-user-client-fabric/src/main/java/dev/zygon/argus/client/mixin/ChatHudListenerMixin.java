package dev.zygon.argus.client.mixin;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j(topic = "Argus-ChatHudListenerMixin")
@Mixin(ChatHudListener.class)
public class ChatHudListenerMixin {


    // [22:22:26] [Render thread/INFO]: Hover Text: §6Location: §b(world) [3082 69 2249]
    // §6Name: §bComradeNickHouseMtA
    // §6Group: §bGlobal
    // [22:22:26] [Render thread/INFO]: Message getString(): §6Enter  §ajbblocker  §bComradeNickHouseMtA  §e[3082 69 2249]  §a[1594m §cSouth East§a]
    // [22:22:26] [Render thread/INFO]: [CHAT] §6Enter  §ajbblocker  §bComradeNickHouseMtA  §e[3082 69 2249]  §a[1594m §cSouth East§a]
    @Inject(at = @At("HEAD"), method = "onChatMessage", cancellable = true)
    public void onChatMessage(MessageType type, Text message, UUID sender, CallbackInfo ci) {
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
        log.info("Hover Text: {}", hoverText);
        log.info("Message getString(): {}", message.getString());
    }
}
