package dev.zygon.argus.client.ui;

import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.connector.customize.ArgusMojangTokenGenerator;
import dev.zygon.argus.client.util.JacksonUtil;
import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import lombok.SneakyThrows;

public enum ArgusWebUi {

    INSTANCE;

    private Javalin javalin;

    public void start() {
        var config = ArgusClientConfig.getActiveConfig();
        if (!config.isWebUiEnabled())
            return;

        javalin = Javalin.create()
                .updateConfig(cfg -> {
                    // TODO remove this after webcontent is also
                    //      served via Javalin
                    cfg.plugins.enableCors(cors ->
                            cors.add(CorsPluginConfig::anyHost));
                })
                .get("/api/host", ctx ->
                        ctx.result(config.getArgusHost()))
                .get("/api/token", ctx ->
                        ctx.result(getTokenString()))
                .start(config.getWebUiPort());
    }

    @SneakyThrows // im a bad programmer B)
    private String getTokenString() {
        var tokenGenerator = ArgusMojangTokenGenerator.INSTANCE;
        var accessToken = tokenGenerator.accessToken();
        return JacksonUtil.stringfyToken(accessToken);
    }

    public void stop() {
        if (javalin != null)
            javalin.stop();
    }
}
