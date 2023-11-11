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
package dev.zygon.argus.auth.repository.impl;

import dev.zygon.argus.auth.*;
import dev.zygon.argus.auth.repository.ArgusBannedUserRepository;
import dev.zygon.argus.auth.repository.ArgusTokenIssueRepository;
import dev.zygon.argus.auth.service.ArgusGroupService;
import dev.zygon.argus.auth.service.ArgusTokenGenerator;
import dev.zygon.argus.namespace.Namespace;
import dev.zygon.argus.user.NamespaceUser;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.UUID;

import static dev.zygon.argus.mutiny.UniExtensions.failIfTrue;

@ApplicationScoped
public class ArgusGeneratedTokenIssueRepository implements ArgusTokenIssueRepository {

    private final ArgusGroupService groups;
    private final ArgusTokenGenerator generator;
    private final ArgusBannedUserRepository bannedUsers;

    public ArgusGeneratedTokenIssueRepository(@RestClient ArgusGroupService groups,
                                              ArgusTokenGenerator generator,
                                              ArgusBannedUserRepository bannedUsers) {
        this.groups = groups;
        this.generator = generator;
        this.bannedUsers = bannedUsers;
    }

    @Override
    public Uni<DualToken> fromMojang(MojangAuthData data, MojangAuthStatus status) {
        return bannedUsers.isUserBanned(status.uuid())
                .plug(failIfTrue(new IllegalArgumentException("Cannot generate Mojang token as this user is banned from the system.")))
                .replaceWith(groups.namespace(data.server()))
                .flatMap(namespace -> issueDualFromMojang(namespace, status));
    }

    @Override
    public Uni<DualToken> fromOneTimePass(OneTimePassword password) {
        return bannedUsers.isUserBanned(password.uuid())
                .plug(failIfTrue(new IllegalArgumentException("Cannot generate One Time Password token as this user is banned from the system.")))
                .replaceWith(issueDualFromNamespaceAndUuid(password.namespace(), password.uuid()));
    }

    @Override
    public Uni<ArgusToken> fromRefresh(ArgusToken refreshToken, NamespaceUser namespaceUser) {
        var namespace = new Namespace(namespaceUser.namespace());
        var user = namespaceUser.user();
        return bannedUsers.isUserBanned(user.uuid())
                .plug(failIfTrue(new IllegalArgumentException("Cannot generate Access token as this user is banned from the system.")))
                .replaceWith(issueFromRefresh(refreshToken, namespace, user.uuid()));
    }

    private Uni<DualToken> issueDualFromMojang(Namespace namespace, MojangAuthStatus status) {
        return issueDualFromNamespaceAndUuid(namespace, status.uuid());
    }

    private Uni<DualToken> issueDualFromNamespaceAndUuid(Namespace namespace, UUID uuid) {
        var refreshToken = generator.generateRefreshToken(uuid, namespace.name());
        return issueFromRefresh(refreshToken, namespace, uuid)
                .map(accessToken -> new DualToken(refreshToken, accessToken));
    }

    private Uni<ArgusToken> issueFromRefresh(ArgusToken refreshToken, Namespace namespace, UUID uuid) {
        return Uni.createFrom()
                .item(refreshToken)
                .flatMap(token -> groups.elected(toHeader(token)))
                .map(elected -> generator.generateAccessToken(uuid, namespace.name(), elected.toPermissions()));
    }

    private String toHeader(ArgusToken token) {
        return "Bearer " + token.token();
    }
}
