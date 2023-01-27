package dev.zygon.argus.auth.configuration;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "argus.auth")
public interface AuthConfiguration {

    @WithDefault("")
    String publicKey();

    @WithDefault("")
    String issuer();

    @WithDefault("3")
    int tokenExpirationMinutes();
}
