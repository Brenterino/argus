package dev.zygon.argus.group.auth;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.user.NamespaceUser;
import io.quarkus.security.UnauthorizedException;

import java.util.Map;
import java.util.UUID;

public interface Authorizer {

    NamespaceUser namespaceUser() throws UnauthorizedException;

    NamespaceUser user(UUID uuid) throws UnauthorizedException;

    Group group(String groupName) throws UnauthorizedException;

    Group group(String groupName, Map<String, Object> metadata);
}
