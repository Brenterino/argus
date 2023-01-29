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
