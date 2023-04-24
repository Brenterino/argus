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
            var originalRequest = chain.request();
            var request = chain.request()
                    .newBuilder()
                    .headers(HeaderUtil.updateHeaders(originalRequest.headers(), token))
                    .build();
            return chain.proceed(request);
        } else {
            return chain.proceed(chain.request());
        }
    }
}
