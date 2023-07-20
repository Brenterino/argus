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

import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.groups.GroupStorage;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(at = @At("RETURN"), method = "getPlayerName", cancellable = true)
    public void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        var config = ArgusClientConfig.getActiveConfig();
        var displays = GroupStorage.INSTANCE.getDisplays();
        var noDisplays = displays == null || displays.isEmpty();
        if (config.shouldShowNameOverwrite() && !noDisplays) {
            var uuid = entry.getProfile().getId();
            var name = entry.getProfile().getName();
            if (displays.containsKey(uuid)) {
                var display = displays.get(uuid);
                var symbol = display.symbol();
                var color = display.color();
                var textBuilder = new StringBuilder();
                if (!symbol.isBlank()) {
                    textBuilder.append("[").append(symbol).append("]");
                }
                textBuilder.append(name);
                var text = new LiteralText(textBuilder.toString())
                        .setStyle(Style.EMPTY.withColor(color.getRGB()));
                cir.setReturnValue(text);
            } else if (config.isOverwriteDefaultNamesEnabled()) {
                var text = new LiteralText(name)
                        .setStyle(Style.EMPTY.withColor(Color.WHITE.getRGB()));
                cir.setReturnValue(text);
            }
        }
    }
}
