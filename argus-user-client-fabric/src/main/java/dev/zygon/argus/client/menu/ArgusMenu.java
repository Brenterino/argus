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

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class ArgusMenu extends Screen {

    protected final Screen parent;

    public ArgusMenu(Screen parent) {
        super(new TranslatableText("argus.menu.title"));
        this.parent = parent;
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    protected void init() {
        var refreshButton = ArgusButtons.createRefreshButton(width - 90, 20,
                button -> {}); // TODO refresh groups
        var configButton = ArgusButtons.createConfigButton(width - 65, 20,
                button -> {}); // TODO open config screen
        var closeButton = ArgusButtons.createCloseButton(width - 40, 20,
                button -> close());
        addDrawableChild(refreshButton);
        addDrawableChild(configButton);
        addDrawableChild(closeButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Render full background
        renderBackground(matrices);

        // Render Buttons
        super.render(matrices, mouseX, mouseY, delta);
    }
}
