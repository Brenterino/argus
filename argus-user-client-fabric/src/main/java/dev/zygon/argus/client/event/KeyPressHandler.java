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
package dev.zygon.argus.client.event;

import dev.zygon.argus.client.config.ArgusClientConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.MessageType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import static net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding;

public class KeyPressHandler {
    private static final String CATEGORY = "key.category.argus";

    private static final int WAYPOINT_DISTANCE_INCREMENT = 1000;
    private static final int MIN_WAYPOINT_DISTANCE = 0;
    private static final int MAX_WAYPOINT_DISTANCE = 100000;

    private static final int YAW_DEGREES_INCREMENT = 1;
    private static final int MIN_YAW_DEGREES = 1;
    private static final int MAX_YAW_DEGREES = 180;

    private final KeyBinding toggleStreamerMode;
    private final KeyBinding toggleColoredNames;
    private final KeyBinding toggleChatLocations;
    private final KeyBinding decreaseWaypointDistance;
    private final KeyBinding increaseWaypointDistance;
    private final KeyBinding decreaseYawSliceDegrees;
    private final KeyBinding increaseYawSliceDegrees;

    public KeyPressHandler() {
        increaseYawSliceDegrees = registerKeyBinding(new KeyBinding("key.argus.yaw.slice.increase",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_9, CATEGORY));
        decreaseYawSliceDegrees = registerKeyBinding(new KeyBinding("key.argus.yaw.slice.decrease",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_7, CATEGORY));
        increaseWaypointDistance = registerKeyBinding(new KeyBinding("key.argus.waypoint.distance.increase",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, CATEGORY));
        decreaseWaypointDistance = registerKeyBinding(new KeyBinding("key.argus.waypoint.distance.decrease",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, CATEGORY));
        toggleChatLocations = registerKeyBinding(new KeyBinding("key.argus.toggle.chat",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_6, CATEGORY));
        toggleColoredNames = registerKeyBinding(new KeyBinding("key.argus.toggle.color",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_5, CATEGORY));
        toggleStreamerMode = registerKeyBinding(new KeyBinding("key.argus.toggle.streaming",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_4, CATEGORY));
    }

    public void onTick(MinecraftClient client) {
        var config = ArgusClientConfig.getActiveConfig();
        var change = false;
        while (toggleStreamerMode.wasPressed()) {
            config.setStreamerModeEnabled(!config.isStreamerModeEnabled());
            dropMessage(client, "key.press.argus.toggle.streaming",
                    toggleText(config.isStreamerModeEnabled()));
            change = true;
        }
        while (toggleColoredNames.wasPressed()) {
            config.setColoredNamesEnabled(!config.isColoredNamesEnabled());
            dropMessage(client, "key.press.argus.toggle.color",
                    toggleText(config.isColoredNamesEnabled()));
            change = true;
        }
        while (toggleChatLocations.wasPressed()) {
            config.setHideChatLocationsEnabled(!config.isHideChatLocationsEnabled());
            dropMessage(client, "key.press.argus.toggle.chat",
                    toggleText(config.isHideChatLocationsEnabled()));
            change = true;
        }
        while (decreaseWaypointDistance.wasPressed()) {
            var nextDistance = Math.max(config.getMaxViewDistance() - WAYPOINT_DISTANCE_INCREMENT,
                    MIN_WAYPOINT_DISTANCE);
            config.setMaxViewDistance(nextDistance);
            dropMessage(client, "key.press.argus.waypoint.distance.decrease", config.getMaxViewDistance() + " m");
            change = true;
        }
        while (increaseWaypointDistance.wasPressed()) {
            var nextDistance = Math.min(config.getMaxViewDistance() + WAYPOINT_DISTANCE_INCREMENT,
                    MAX_WAYPOINT_DISTANCE);
            config.setMaxViewDistance(nextDistance);
            dropMessage(client, "key.press.argus.waypoint.distance.increase", config.getMaxViewDistance() + " m");
            change = true;
        }
        while (decreaseYawSliceDegrees.wasPressed()) {
            var nextDegrees = Math.max(config.getYawSliceDegrees() - YAW_DEGREES_INCREMENT,
                    MIN_YAW_DEGREES);
            config.setYawSliceDegrees(nextDegrees);
            dropMessage(client, "key.press.argus.yaw.slice.decrease", config.getYawSliceDegrees() + " deg");
            change = true;
        }
        while (increaseYawSliceDegrees.wasPressed()) {
            var nextDegrees = Math.min(config.getYawSliceDegrees() + YAW_DEGREES_INCREMENT,
                    MAX_YAW_DEGREES);
            config.setYawSliceDegrees(nextDegrees);
            dropMessage(client, "key.press.argus.yaw.slice.increase", config.getYawSliceDegrees() + " deg");
            change = true;
        }
        if (change) {
            var holder = AutoConfig.getConfigHolder(ArgusClientConfig.class);
            holder.save();
        }
    }

    private TranslatableText toggleText(boolean toggle) {
        return new TranslatableText(toggle ? "key.press.argus.toggle.on" : "key.press.argus.toggle.off");
    }

    private void dropMessage(MinecraftClient client, String label, String additionalText) {
        var text = new TranslatableText(label)
                .append(additionalText);
        client.inGameHud.addChatMessage(MessageType.SYSTEM, text, Util.NIL_UUID);
    }

    private void dropMessage(MinecraftClient client, String label, TranslatableText additionalText) {
        var text = new TranslatableText(label)
                .append(additionalText);
        client.inGameHud.addChatMessage(MessageType.SYSTEM, text, Util.NIL_UUID);
    }
}
