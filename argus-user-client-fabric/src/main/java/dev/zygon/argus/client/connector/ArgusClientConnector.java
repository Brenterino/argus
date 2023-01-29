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
package dev.zygon.argus.client.connector;

import dev.zygon.argus.client.ArgusClient;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.connector.customize.ArgusModClientCustomizer;
import dev.zygon.argus.client.connector.customize.ArgusMojangTokenGenerator;
import dev.zygon.argus.client.scheduler.ClientScheduler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public enum ArgusClientConnector {

    INSTANCE;

    private final ArgusClient client;
    private ScheduledFuture<?> tokenRefresh;

    ArgusClientConnector() {
        client = new ArgusClient(ArgusModClientCustomizer.INSTANCE);
    }

    public void open(String server, String username) {
        log.info("[ARGUS] Connected to server {}. Starting Argus Client.", server);
        var clientConfig = ArgusClientConfig.getActiveConfig();
        var tokenGenerator = ArgusMojangTokenGenerator.INSTANCE;
        client.init(clientConfig.getArgusHost());
        tokenGenerator.setAuth(client.getAuth());
        tokenGenerator.setServer(server);
        tokenGenerator.setUsername(username);
        tokenRefresh = ClientScheduler.INSTANCE
                .register(tokenGenerator::refresh,
                        clientConfig.getRefreshTokenCheckIntervalSeconds(), TimeUnit.SECONDS);
    }

    public void close() {
        if (tokenRefresh != null && !tokenRefresh.isDone()) {
            log.info("[ARGUS] Disconnected from server, cleaning up...");

            tokenRefresh.cancel(false);
        }
    }
}
