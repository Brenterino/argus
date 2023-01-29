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

import dev.zygon.argus.auth.ArgusToken;
import dev.zygon.argus.auth.MojangAuthData;
import dev.zygon.argus.auth.MojangAuthStatus;
import dev.zygon.argus.auth.repository.ArgusTokenIssueRepository;
import dev.zygon.argus.auth.service.ArgusGroupService;
import dev.zygon.argus.auth.service.ArgusTokenGenerator;
import dev.zygon.argus.namespace.Namespace;
import dev.zygon.argus.permission.Permissions;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;

@ApplicationScoped
public class ArgusGeneratedTokenIssueRepository implements ArgusTokenIssueRepository {

    private final ArgusGroupService groups;
    private final ArgusTokenGenerator generator;

    public ArgusGeneratedTokenIssueRepository(@RestClient ArgusGroupService groups,
                                              ArgusTokenGenerator generator) {
        this.groups = groups;
        this.generator = generator;
    }

    @Override
    public Uni<ArgusToken> issue(MojangAuthData data, MojangAuthStatus status) {
        return groups.namespace(data.server())
                .flatMap(namespace -> issueWithNamespace(namespace, status));
    }

    private Uni<ArgusToken> issueWithNamespace(Namespace namespace, MojangAuthStatus status) {
        var emptyPermissions = new Permissions(Collections.emptyMap());
        return Uni.createFrom()
                .item(generator.generate(status.uuid(), namespace.name(), emptyPermissions))
                .flatMap(token -> groups.elected(toHeader(token)))
                .map(elected -> generator.generate(status.uuid(), namespace.name(), elected.toPermissions()));
    }

    private String toHeader(ArgusToken token) {
        return "Bearer " + token.token();
    }
}
