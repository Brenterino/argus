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
import dev.zygon.argus.client.auth.TokenGenerator;
import lombok.Getter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.Closeable;

public class ArgusClient implements Closeable {

    private final OkHttpClient client;
    private @Getter ArgusAuthApi auth;
    private @Getter ArgusGroupApi groups;
    private @Getter ArgusAuditApi audit;
    private @Getter ArgusPermissionApi permissions;

    public ArgusClient(TokenGenerator tokenGenerator) {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenGenerator))
                .build();
    }

    public void init(String argusBaseUrl) {
        var retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(argusBaseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        this.auth = retrofit.create(ArgusAuthApi.class);
        this.groups = retrofit.create(ArgusGroupApi.class);
        this.audit = retrofit.create(ArgusAuditApi.class);
        this.permissions = retrofit.create(ArgusPermissionApi.class);
    }

    @Override
    public void close() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}
