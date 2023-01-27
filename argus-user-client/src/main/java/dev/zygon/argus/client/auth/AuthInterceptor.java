package dev.zygon.argus.client.auth;

import dev.zygon.argus.client.util.HeaderUtil;
import lombok.NonNull;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public class AuthInterceptor implements Interceptor {

    private final TokenGenerator generator;

    public AuthInterceptor(TokenGenerator generator) {
        this.generator = generator;
    }

    @Override
    public @NonNull Response intercept(@NonNull Chain chain) throws IOException {
        var token = generator.token();
        if (token != null) {
            var request = chain.request()
                    .newBuilder()
                    .headers(HeaderUtil.createHeaders(token))
                    .build();
            return chain.proceed(request);
        } else {
            return chain.proceed(chain.request());
        }
    }
}
