package dev.zygon.argus.client.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.Headers;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeaderUtil {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    public static Headers createHeaders(String token) {
        return Headers.of(AUTHORIZATION_HEADER, "Bearer " + token);
    }
}
