package dev.zygon.argus.client;

import dev.zygon.argus.client.auth.TokenGenerator;
import dev.zygon.argus.client.util.HeaderUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

import java.io.Closeable;
import java.io.IOException;

public class ArgusLocationsClient implements Closeable {

    private final WebSocket socket;

    ArgusLocationsClient(OkHttpClient client, TokenGenerator tokenGenerator) {
        this.socket = openSocket(client, tokenGenerator);
    }

    private WebSocket openSocket(OkHttpClient client, TokenGenerator tokenGenerator) {
        var token = tokenGenerator.token();
        if (token == null) {
            throw new IllegalStateException("Must have a token available to open a web socket.");
        }
        var request = new Request.Builder()
                .headers(HeaderUtil.createHeaders(token))
                .build();
        // TODO create websocket listener
        return client.newWebSocket(request, null);
    }

    @Override
    public void close() throws IOException {
        socket.close(1000, "Client initiated close.");
    }
}
