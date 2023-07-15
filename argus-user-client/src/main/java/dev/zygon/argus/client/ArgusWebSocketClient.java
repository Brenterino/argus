package dev.zygon.argus.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.zygon.argus.client.auth.TokenGenerator;
import dev.zygon.argus.client.util.HeaderUtil;
import dev.zygon.argus.client.util.JacksonUtil;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.*;

import java.io.Closeable;
import java.util.function.Consumer;

public class ArgusWebSocketClient<T> implements Closeable {

    // Mutable
    private WebSocket socket;
    private Consumer<T> listeners;
    private Consumer<Throwable> errorHandler;
    @Getter
    private boolean closed = true;

    // Immutable
    private final OkHttpClient client;
    private final ArgusClientCustomizer customizer;
    private final ObjectMapper mapper;
    private final WebSocketListener socketListener;
    private final Class<T> dataClazz;

    ArgusWebSocketClient(OkHttpClient client, ArgusClientCustomizer customizer, Class<T> dataClazz) {
        this.client = client;
        this.customizer = customizer;
        this.mapper = JacksonUtil.jsr310ObjectMapper();
        this.socketListener = new WebSocketListenerImpl();
        this.errorHandler = e -> {};
        this.dataClazz = dataClazz;
    }

    public void init(String argusBaseUrl, String endpoint) {
        if (!closed) {
            close();
        }
        // use ws instead of http, will make https -> wss as well
        this.socket = openSocket(argusBaseUrl, endpoint, client, customizer.tokenGenerator());
    }

    private WebSocket openSocket(String baseUrl, String endpoint, OkHttpClient client, TokenGenerator tokenGenerator) {
        var token = tokenGenerator.token();
        if (token == null) {
            throw new IllegalStateException("Must have a token available to open a web socket.");
        }
        var request = new Request.Builder()
                .url(baseUrl + endpoint)
                .headers(HeaderUtil.createHeaders(token)) // TODO we might not even need to attach the token at this point, verify this assumption
                .build();
        return client.newWebSocket(request, socketListener);
    }

    public void addListener(Consumer<T> listener) {
        if (listeners != null) {
            listeners = listeners.andThen(listener);
        } else {
            listeners = listener;
        }
    }

    public void addErrorHandler(Consumer<Throwable> handler) {
        errorHandler = errorHandler.andThen(handler);
    }

    public void send(T data) {
        if (!closed) {
            try {
                var json = mapper.writeValueAsString(data);
                socket.send(json);
            } catch (Exception e) {
                errorHandler.accept(e);
            }
        }
    }

    @Override
    public void close() {
        if (!closed) {
            socket.close(1000, "Client initiated close.");
            closed = true;
        }
    }

    private class WebSocketListenerImpl extends WebSocketListener {

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            closed = false;
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            if (listeners != null) {
                try {
                    var data = mapper.readValue(text, dataClazz);
                    listeners.accept(data);
                } catch (Exception e) {
                    errorHandler.accept(e);
                }
            }
        }

        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            closed = true;
        }

        @Override
        public void onFailure(@NonNull WebSocket socket, @NonNull Throwable t, Response response) {
            errorHandler.accept(t);
            closed = true;
        }

        @Override
        public void onClosed(@NonNull WebSocket socket, int code, @NonNull String reason) {
            closed = true;
        }
    }
}
