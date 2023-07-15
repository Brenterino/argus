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
import dev.zygon.argus.client.event.RemoteLocationHandler;
import dev.zygon.argus.client.event.RemoteStatusHandler;
import dev.zygon.argus.client.groups.GroupStorage;
import dev.zygon.argus.client.location.LocationStorage;
import dev.zygon.argus.client.scheduler.ClientScheduler;
import dev.zygon.argus.client.status.StatusStorage;
import dev.zygon.argus.client.ui.ArgusWebUi;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public enum ArgusClientConnector {

    INSTANCE;

    private final ArgusClient client;
    private ScheduledFuture<?> tokenRefresh;
    private ScheduledFuture<?> membershipRefresh;
    private ScheduledFuture<?> electionsRefresh;
    private ScheduledFuture<?> locationKeepAlive;
    private ScheduledFuture<?> locationRemoteSync;
    private ScheduledFuture<?> locationCleaner;
    private ScheduledFuture<?> statusKeepAlive;
    private ScheduledFuture<?> statusRemoteSync;

    ArgusClientConnector() {
        client = new ArgusClient(ArgusModClientCustomizer.INSTANCE);
    }

    public void open(String server, String username) {
        log.info("[ARGUS] Connected to server {}. Starting Argus Client.", server);

        // Initialize client with host
        var config = ArgusClientConfig.getActiveConfig();
        client.init(config.getArgusHost());

        // Initialize Subcomponents
        init(server, username);

        // Initialize Web UI
        ArgusWebUi.INSTANCE.start();
    }

    private void init(String server, String username) {
        var config = ArgusClientConfig.getActiveConfig();
        var tokenGenerator = ArgusMojangTokenGenerator.INSTANCE;
        tokenGenerator.setAuth(client.getAuth());
        tokenGenerator.setServer(server);
        tokenGenerator.setUsername(username);
        tokenRefresh = ClientScheduler.INSTANCE
                .register(tokenGenerator::refresh,
                        config.getRefreshTokenCheckIntervalSeconds(), TimeUnit.SECONDS);

        tokenGenerator.onNextRefresh(() -> {
            initGroupRefresh();
            initLocationTracking();
            initStatusTracking();
        });
    }

    private void initGroupRefresh() {
        var config = ArgusClientConfig.getActiveConfig();
        var groupStorage = GroupStorage.INSTANCE;
        groupStorage.setGroups(client.getGroups());
        groupStorage.setPermissions(client.getPermissions());
        membershipRefresh = ClientScheduler.INSTANCE
                .register(groupStorage::refreshMemberships, config.getRefreshMembershipIntervalSeconds(), TimeUnit.SECONDS);
        electionsRefresh = ClientScheduler.INSTANCE
                .register(groupStorage::refreshElections, config.getRefreshElectionsIntervalSeconds(), TimeUnit.SECONDS);
    }

    private void initLocationTracking() {
        var config = ArgusClientConfig.getActiveConfig();
        var locations = client.getLocations();
        var remoteHandler = RemoteLocationHandler.INSTANCE;
        remoteHandler.setLocations(client.getLocations());
        locations.addListener(remoteHandler::onLocationsReceived);
        locations.addErrorHandler(remoteHandler::onRemoteSyncFailure);
        locationKeepAlive = ClientScheduler.INSTANCE
                .register(remoteHandler::keepClientAlive,
                        config.getRefreshSocketClientIntervalSeconds(), TimeUnit.SECONDS);
        locationRemoteSync = ClientScheduler.INSTANCE
                .registerWithDelay(() -> LocationStorage.INSTANCE.syncRemote(client),
                        config.getTransmitInitialWaitForConnectionSeconds() * 1000L,
                        config.getTransmitLocationsIntervalMillis(), TimeUnit.MILLISECONDS);
        locationCleaner = ClientScheduler.INSTANCE
                .register(LocationStorage.INSTANCE::cleanLocations,
                        config.getCleanLocationsIntervalSeconds(), TimeUnit.SECONDS);
    }

    private void initStatusTracking() {
        var config = ArgusClientConfig.getActiveConfig();
        var statuses = client.getStatuses();
        var remoteHandler = RemoteStatusHandler.INSTANCE;
        remoteHandler.setStatuses(statuses);
        statuses.addListener(remoteHandler::onStatusReceived);
        statuses.addErrorHandler(remoteHandler::onRemoteSyncFailure);
        statusKeepAlive = ClientScheduler.INSTANCE
                .register(remoteHandler::keepClientAlive,
                        config.getRefreshSocketClientIntervalSeconds(), TimeUnit.SECONDS);
        statusRemoteSync = ClientScheduler.INSTANCE
                .registerWithDelay(() -> StatusStorage.INSTANCE.syncRemote(client),
                        config.getTransmitInitialWaitForConnectionSeconds() * 1000L,
                        config.getTransmitStatusIntervalMillis(), TimeUnit.MILLISECONDS);
    }

    public void close() {
        log.info("[ARGUS] Disconnected from server, cleaning up...");
        safeCancel(tokenRefresh);
        safeCancel(membershipRefresh);
        safeCancel(electionsRefresh);
        safeCancel(locationKeepAlive);
        safeCancel(locationRemoteSync);
        safeCancel(locationCleaner);
        safeCancel(statusKeepAlive);
        safeCancel(statusRemoteSync);

        client.getLocations().close();
        client.getStatuses().close();
        ArgusMojangTokenGenerator.INSTANCE.clean();
        GroupStorage.INSTANCE.clean();
        LocationStorage.INSTANCE.clean();
        ArgusWebUi.INSTANCE.stop();
    }

    private void safeCancel(ScheduledFuture<?> future) {
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }
}
