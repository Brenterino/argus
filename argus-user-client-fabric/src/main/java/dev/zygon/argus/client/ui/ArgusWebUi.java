package dev.zygon.argus.client.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.connector.customize.ArgusMojangTokenGenerator;
import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import lombok.SneakyThrows;

public enum ArgusWebUi {

    INSTANCE;

    private Javalin javalin;
    private final ObjectMapper mapper;

    ArgusWebUi() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

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
        return mapper.writeValueAsString(accessToken);
    }

    public void stop() {
        if (javalin != null)
            javalin.stop();
    }
}
