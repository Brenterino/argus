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
package dev.zygon.argus.client.menu;

import dev.zygon.argus.client.ArgusFabricClient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.util.Identifier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArgusButtons {

    private static final int BUTTON_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_TEXTURE_WIDTH = 20;
    private static final int BUTTON_TEXTURE_HEIGHT = 60;
    private static final int BASE_TEXTURE_OFFSET_U = 0;
    private static final int BASE_TEXTURE_OFFSET_V = 0;
    private static final int HOVER_TEXTURE_OFFSET_V = 20;

    private static final Identifier REFRESH_BUTTON_TEXTURE =
            new Identifier(ArgusFabricClient.MOD_ID, "textures/gui/refresh_button.png");
    private static final Identifier CONFIG_BUTTON_TEXTURE =
            new Identifier(ArgusFabricClient.MOD_ID, "textures/gui/config_button.png");
    private static final Identifier CLOSE_BUTTON_TEXTURE =
            new Identifier(ArgusFabricClient.MOD_ID, "textures/gui/close_button.png");

    public static TexturedButtonWidget createRefreshButton(int x, int y, PressAction onPress) {
        return createButton(x, y, REFRESH_BUTTON_TEXTURE, onPress);
    }

    public static TexturedButtonWidget createConfigButton(int x, int y, PressAction onPress) {
        return createButton(x, y, CONFIG_BUTTON_TEXTURE, onPress);
    }

    public static TexturedButtonWidget createCloseButton(int x, int y, PressAction onPress) {
        return createButton(x, y, CLOSE_BUTTON_TEXTURE, onPress);
    }

    private static TexturedButtonWidget createButton(int x, int y, Identifier texture, PressAction onPress) {
        return new TexturedButtonWidget(x, y,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                BASE_TEXTURE_OFFSET_U, BASE_TEXTURE_OFFSET_V, HOVER_TEXTURE_OFFSET_V,
                texture, BUTTON_TEXTURE_WIDTH, BUTTON_TEXTURE_HEIGHT,
                onPress);
    }
}
