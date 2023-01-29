package dev.zygon.argus.group.profile;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class IntegrationProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                // Logging
                "quarkus.log.level", "DEBUG",

                // Verify JWT
                "argus.auth.public-key", "public.pem",
                "argus.auth.issuer", "https://argus.zygon.dev/issuer",

                // Sign JWT
                "smallrye.jwt.sign.key.location", "private.pem"
        );
    }
}
