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
package dev.zygon.argus.auth.service.impl;

import dev.zygon.argus.auth.service.ArgusTokenReader;
import dev.zygon.argus.user.NamespaceUser;
import dev.zygon.argus.user.User;
import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.RequestScoped;
import java.util.Optional;
import java.util.UUID;

import static java.util.function.Predicate.not;

@Slf4j
@RequestScoped
public class ArgusJwtTokenReader implements ArgusTokenReader {

    private static final String UPN_UUID_NAMESPACE_SPLIT = "@";
    private static final int UUID_INDEX = 0;
    private static final int NAMESPACE_INDEX = 1;

    private final JsonWebToken token;

    public ArgusJwtTokenReader(JsonWebToken token) {
        this.token = token;
    }

    @Override
    public String rawToken() {
        return token.getRawToken();
    }

    @Override
    public boolean isAccessToken() {
        return !token.getGroups()
                .isEmpty();
    }

    @Override
    public NamespaceUser namespaceUser() {
        var upn = extractUpn();
        var splitUpn = upn.split(UPN_UUID_NAMESPACE_SPLIT);
        var uuid = UUID.fromString(splitUpn[UUID_INDEX]);
        var namespace = splitUpn[NAMESPACE_INDEX];
        return namespaceUser(namespace, uuid);
    }

    private NamespaceUser namespaceUser(String namespace, UUID uuid) {
        var user = new User(uuid, "");
        return new NamespaceUser(namespace, user);
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
