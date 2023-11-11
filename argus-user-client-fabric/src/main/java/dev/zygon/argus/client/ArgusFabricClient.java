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
package dev.zygon.argus.client;

import dev.zygon.argus.client.command.ArgusClientCommandHandler;
import dev.zygon.argus.client.command.ArgusFabricClientCommands;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.event.KeyPressHandler;
import dev.zygon.argus.client.status.UserStatusChecker;
import lombok.extern.slf4j.Slf4j;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.ActionResult;

@Slf4j
public class ArgusFabricClient implements ClientModInitializer {

    public static final String MOD_ID = "argus";

    @Override
    public void onInitializeClient() {
        log.info("[ARGUS] Argus is loading.");
        var keyPressHandler = new KeyPressHandler();
        var holder = AutoConfig.register(ArgusClientConfig.class,
                JanksonConfigSerializer::new);
        holder.registerSaveListener((save, x) -> {
            ArgusClientConfig.setActiveConfig(save.getConfig());
            return ActionResult.SUCCESS;
        });
        ArgusClientConfig.setActiveConfig(holder.getConfig());
        ClientTickEvents.END_CLIENT_TICK.register(keyPressHandler::onTick);
        ClientTickEvents.END_CLIENT_TICK.register(UserStatusChecker.INSTANCE::onTick);
        ArgusFabricClientCommands.registerCommands();
    }
}
