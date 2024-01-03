package dev.zygon.argus.client.ui;

import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.connector.customize.ArgusMojangTokenGenerator;
import dev.zygon.argus.client.util.JacksonUtil;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

public enum ArgusWebUi {

    INSTANCE;

    private Javalin javalin;

    public void start() {
        var config = ArgusClientConfig.getActiveConfig();
        if (!config.isWebUiEnabled())
            return;

        javalin = Javalin.create()
                .updateConfig(cfg -> cfg.staticFiles.add("/public", Location.CLASSPATH))
                .get("/", ctx -> {
                    ctx.contentType(ContentType.TEXT_HTML);
                    var index = ArgusWebUi.class.getResourceAsStream("/public/index.html");
                    if (index != null) { // not sure why but have to explicitly load index for /
                        ctx.status(HttpStatus.OK);
                        ctx.result(index);
                    } else {
                        ctx.status(HttpStatus.NOT_FOUND);
                        ctx.result("Could not load index page for some reason!");
                    }
                })
                .get("/api/host", ctx ->
                        ctx.result(config.getArgusHost()))
                .get("/api/token", ctx ->
                        ctx.result(getTokenString()))
                .start(config.getWebUiPort());

        var minecraft = MinecraftClient.getInstance();

        // Drop a nice message so you can just clicky :)
        var url = "http://localhost:" + config.getWebUiPort() + "/";
        var clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        var urlText = new LiteralText(url)
                .setStyle(Style.EMPTY
                        .withClickEvent(clickEvent)
                        .withColor(0x54FCFC)
                        .withUnderline(true));
        var text = new TranslatableText("text.webui.argus.chatMessage")
                .append(" ")
                .append(urlText);
        minecraft.inGameHud.addChatMessage(MessageType.SYSTEM,
                text, Util.NIL_UUID);
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
