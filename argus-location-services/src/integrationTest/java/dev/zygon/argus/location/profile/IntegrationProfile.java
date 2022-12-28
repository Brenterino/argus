package dev.zygon.argus.location.profile;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class IntegrationProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                // Logging
                "quarkus.log.level", "DEBUG",

                // Verify JWT
                "mp.jwt.verify.publickey.location", "public.pem",
                "mp.jwt.verify.issuer", "https://argus.zygon.dev/issuer",

                // Sign JWT
                "smallrye.jwt.sign.key.location", "private.pem"
        );
    }
}
