package dev.zygon.argus.location.auth;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.user.Permissions;
import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.RequestScoped;
import javax.websocket.Session;
import java.util.stream.Stream;

@Slf4j
@RequestScoped
public class JwtSessionAuthorizer implements SessionAuthorizer {

    private static final String PERMISSION_ATTRIBUTE_NAME = "PERMISSIONS";

    private final JsonWebToken token;

    public JwtSessionAuthorizer(JsonWebToken token) {
        this.token = token;
    }

    @Override
    public boolean authorize(Session session) {
        try {
            authorizeInternal(session);
            return true;
        } catch (UnauthorizedException e) {
            log.warn("Session with ID ({}) could not be authorized for access.",
                    session.getId(), e);
            return false;
        }
    }

    private void authorizeInternal(Session session) throws UnauthorizedException {
        if (token != null) {
            extractAndAttachPermissions(session, token);
        } else {
            throw new UnauthorizedException("There is not JWT attached to this session.");
        }
    }

    private void extractAndAttachPermissions(Session session, JsonWebToken token) {
        var permissions = Permissions.fromRaw(token.getGroups());
        var groupPermissions = permissions.permissions();
        if (!groupPermissions.isEmpty()) {
            var sessionProperties = session.getUserProperties();
            sessionProperties.put(PERMISSION_ATTRIBUTE_NAME, permissions);
        } else {
            throw new UnauthorizedException("Attached JWT did not have any group claims.");
        }
    }

    @Override
    public Stream<Group> readGroups(Session session) {
        var properties = session.getUserProperties();
        var rawPermissions = properties.getOrDefault(PERMISSION_ATTRIBUTE_NAME, null);
        if (rawPermissions instanceof Permissions permissions) {
            return permissions.readGroups()
                    .stream();
        } else {
            return Stream.empty();
        }
    }

    @Override
    public Stream<Group> writeGroups(Session session) {
        var properties = session.getUserProperties();
        var rawPermissions = properties.getOrDefault(PERMISSION_ATTRIBUTE_NAME, null);
        if (rawPermissions instanceof Permissions permissions) {
            return permissions.writeGroups()
                    .stream();
        } else {
            return Stream.empty();
        }
    }
}
