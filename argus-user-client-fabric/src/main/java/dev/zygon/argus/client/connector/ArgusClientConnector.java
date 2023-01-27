package dev.zygon.argus.client.connector;

import dev.zygon.argus.client.ArgusClient;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.scheduler.ClientScheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public enum ArgusClientConnector {

    INSTANCE;

    private final ArgusClient client;
    private ScheduledFuture<?> tokenRefresh;

    ArgusClientConnector() {
        client = new ArgusClient(ArgusMojangTokenGenerator.INSTANCE);
    }

    public void open(String server, String username) {
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
            tokenRefresh.cancel(false);
        }
    }
}
