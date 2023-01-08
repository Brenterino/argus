package dev.zygon.argus.client.auth;

import lombok.NonNull;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public class AuthInterceptor implements Interceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public @NonNull Response intercept(@NonNull Chain chain) throws IOException {
        // TODO request token if expired, attach if present
        var request = chain.request()
                .newBuilder()
                .addHeader(AUTHORIZATION_HEADER, "")
                .build();
        return chain.proceed(request);
    }
}
