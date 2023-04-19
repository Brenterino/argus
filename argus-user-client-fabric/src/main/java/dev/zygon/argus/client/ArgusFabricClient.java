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

import dev.zygon.argus.client.config.ArgusClientConfig;
import lombok.extern.slf4j.Slf4j;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

@Slf4j
public class ArgusFabricClient implements ClientModInitializer {

    public static final String MOD_ID = "argus";

    @Override
    public void onInitializeClient() {
        log.info("[ARGUS] Argus is loading.");
        var holder = AutoConfig.register(ArgusClientConfig.class,
                JanksonConfigSerializer::new);
        ArgusClientConfig.setActiveConfig(holder.getConfig());
    }
}
