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

import dev.zygon.argus.client.api.ArgusAuditApi;
import dev.zygon.argus.client.api.ArgusAuthApi;
import dev.zygon.argus.client.api.ArgusGroupApi;
import dev.zygon.argus.client.api.ArgusPermissionApi;
import dev.zygon.argus.client.auth.AuthInterceptor;
import dev.zygon.argus.client.util.JacksonUtil;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.status.UserStatus;
import lombok.Getter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import java.io.Closeable;

public class ArgusClient implements Closeable {

    private final OkHttpClient client;
    private @Getter ArgusAuthApi auth;
    private @Getter ArgusGroupApi groups;
    private @Getter ArgusAuditApi audit;
    private @Getter ArgusPermissionApi permissions;
    private final @Getter ArgusWebSocketClient<Locations> locations;
    private final @Getter ArgusWebSocketClient<UserStatus> statuses;

    public ArgusClient(ArgusClientCustomizer customizer) {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(customizer.tokenGenerator()))
                .hostnameVerifier(customizer.hostnameVerifier())
                .sslSocketFactory(customizer.sslSocketFactory(),
                        customizer.trustManager())
                .build();
        this.locations = new ArgusWebSocketClient<>(client, customizer, Locations.class);
        this.statuses = new ArgusWebSocketClient<>(client, customizer, UserStatus.class);
    }

    public void init(String argusBaseUrl) {
        var retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(argusBaseUrl)
                .addConverterFactory(JacksonUtil.converterFactory())
                .build();
        this.auth = retrofit.create(ArgusAuthApi.class);
        this.groups = retrofit.create(ArgusGroupApi.class);
        this.audit = retrofit.create(ArgusAuditApi.class);
        this.permissions = retrofit.create(ArgusPermissionApi.class);
    }

    @Override
    public void close() {
        locations.close();
        statuses.close();
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}
