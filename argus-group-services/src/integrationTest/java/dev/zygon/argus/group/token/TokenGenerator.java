package dev.zygon.argus.group.token;

import io.smallrye.jwt.build.Jwt;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class TokenGenerator {

    private static final String UPN_UUID_NAMESPACE_SPLIT = "@";

    private final String issuer;

    public TokenGenerator(
            @ConfigProperty(name = "argus.auth.issuer") String issuer) {
        this.issuer = issuer;
    }

    public String bearer(UUID uuid, String namespace) {
        return "Bearer " + token(uuid, namespace);
    }

    protected String token(UUID uuid, String namespace) {
        var upn = uuid.toString() +
                UPN_UUID_NAMESPACE_SPLIT +
                namespace;
        return token(upn);
    }

    protected String token(String upn) {
        var expiration = Instant.now(Clock.systemUTC())
                .plus(5, ChronoUnit.MINUTES);
        return Jwt.issuer(issuer)
                .upn(upn)
                .expiresAt(expiration)
                .sign();
    }
}
