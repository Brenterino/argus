package dev.zygon.argus.group.auth;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.user.NamespaceUser;
import dev.zygon.argus.user.User;
import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.RequestScoped;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.function.Predicate.not;

@Slf4j
@RequestScoped
public class JwtAuthorizer implements Authorizer {

    private static final String UPN_UUID_NAMESPACE_SPLIT = "@";
    private static final int UUID_INDEX = 0;
    private static final int NAMESPACE_INDEX = 1;

    private final JsonWebToken token;

    public JwtAuthorizer(JsonWebToken token) {
        this.token = token;
    }

    @Override
    public NamespaceUser namespaceUser() throws UnauthorizedException {
        var upn = extractUpn();
        var splitUpn = upn.split(UPN_UUID_NAMESPACE_SPLIT);
        var uuid = UUID.fromString(splitUpn[UUID_INDEX]);
        var namespace = splitUpn[NAMESPACE_INDEX];
        return namespaceUser(namespace, uuid);
    }

    @Override
    public NamespaceUser user(UUID uuid) throws UnauthorizedException {
        var upn = extractUpn();
        var splitUpn = upn.split(UPN_UUID_NAMESPACE_SPLIT);
        var namespace = splitUpn[NAMESPACE_INDEX];
        return namespaceUser(namespace, uuid);
    }

    private NamespaceUser namespaceUser(String namespace, UUID uuid) {
        var user = new User(uuid, "");
        return new NamespaceUser(namespace, user);
    }

    @Override
    public Group group(String groupName) throws UnauthorizedException {
        return group(groupName, Collections.emptyMap());
    }

    @Override
    public Group group(String groupName, Map<String, Object> metadata) {
        var upn = extractUpn();
        var splitUpn = upn.split(UPN_UUID_NAMESPACE_SPLIT);
        var namespace = splitUpn[NAMESPACE_INDEX];
        return new Group(namespace, groupName, metadata);
    }

    private String extractUpn() {
        var actualToken = extractToken();
        return actualToken.<String>claim(Claims.upn)
                .filter(not(String::isEmpty))
                .filter(not(String::isBlank))
                .filter(upn -> upn.contains(UPN_UUID_NAMESPACE_SPLIT))
                .orElseThrow(() -> new UnauthorizedException("The attached JWT does not claim a valid UPN."));
    }

    private JsonWebToken extractToken() {
        return Optional.ofNullable(token)
                .orElseThrow(() -> new UnauthorizedException("There is no JWT attached to this request."));
    }
}
