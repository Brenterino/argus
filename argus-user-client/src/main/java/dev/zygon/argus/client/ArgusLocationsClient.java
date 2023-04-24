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

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.zygon.argus.client.auth.TokenGenerator;
import dev.zygon.argus.client.util.HeaderUtil;
import dev.zygon.argus.location.Locations;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.*;

import java.io.Closeable;
import java.util.function.Consumer;

public class ArgusLocationsClient implements Closeable {

    // Mutable
    private WebSocket socket;
    private Consumer<Locations> listeners;
    private Consumer<Throwable> errorHandler;
    @Getter private boolean closed = true;

    // Immutable
    private final OkHttpClient client;
    private final ArgusClientCustomizer customizer;
    private final ObjectMapper mapper;
    private final WebSocketListener socketListener;

    ArgusLocationsClient(OkHttpClient client, ArgusClientCustomizer customizer) {
        this.client = client;
        this.customizer = customizer;
        this.mapper = new ObjectMapper();
        this.socketListener = new WebSocketListenerImpl();
        this.errorHandler = e -> {};
    }

    public void init(String argusBaseUrl) {
        if (!closed) {
            close();
        }
        // use ws instead of http, will make https -> wss as well
        this.socket = openSocket(argusBaseUrl, client, customizer.tokenGenerator());
    }

    private WebSocket openSocket(String baseUrl, OkHttpClient client, TokenGenerator tokenGenerator) {
        var token = tokenGenerator.token();
        if (token == null) {
            throw new IllegalStateException("Must have a token available to open a web socket.");
        }
        var request = new Request.Builder()
                .url(baseUrl + "/locations")
                .headers(HeaderUtil.createHeaders(token)) // TODO we might not even need to attach the token at this point, verify this assumption
                .build();
        return client.newWebSocket(request, socketListener);
    }

    public void addListener(Consumer<Locations> listener) {
        if (listeners != null) {
            listeners = listeners.andThen(listener);
        } else {
            listeners = listener;
        }
    }

    public void addErrorHandler(Consumer<Throwable> handler) {
        errorHandler = errorHandler.andThen(handler);
    }

    public void sendLocations(Locations data) {
        if (closed) {
            throw new IllegalStateException("Cannot send location data because socket is closed.");
        }
        try {
            var json = mapper.writeValueAsString(data);
            socket.send(json);
        } catch (Exception e) {
            errorHandler.accept(e);
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
                    var locations = mapper.readValue(text, Locations.class);
                    listeners.accept(locations);
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
