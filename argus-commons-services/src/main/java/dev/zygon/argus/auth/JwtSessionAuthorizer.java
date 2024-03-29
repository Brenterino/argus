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
package dev.zygon.argus.auth;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.permission.Permissions;
import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.RequestScoped;
import jakarta.websocket.Session;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Implementation of {@link SessionAuthorizer} which requires the principal
 * attached to the session to be of the JWT (JSON Web Token) variety. Access to
 * resources would be determined by reviewing the groups attached.
 *
 * @see SessionAuthorizer
 */
@Slf4j
@RequestScoped
public class JwtSessionAuthorizer implements SessionAuthorizer {

    protected static final String PERMISSION_ATTRIBUTE_NAME = "PERMISSIONS";

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
            log.warn("Session with ID ({}) could not be authorized for access. Reason: {}",
                    session.getId(), e.getMessage());
            return false;
        }
    }

    private void authorizeInternal(Session session) throws UnauthorizedException {
        var actualToken = Optional.ofNullable(token)
                .orElseThrow(() -> new UnauthorizedException("There is no JWT attached to this session."));
        extractAndAttachPermissions(session, actualToken);
    }

    private void extractAndAttachPermissions(Session session, JsonWebToken token) {
        var permissions = token.<Set<String>>claim(Claims.groups)
                .map(Permissions::fromRaw)
                .orElseThrow(() -> new UnauthorizedException("The attached JWT does not claim a valid UPN."));
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
