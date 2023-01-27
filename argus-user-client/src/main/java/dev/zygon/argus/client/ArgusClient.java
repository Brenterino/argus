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
