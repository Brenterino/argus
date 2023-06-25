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

import dev.zygon.argus.auth.repository.ArgusTokenIssueRepository;
import dev.zygon.argus.auth.service.ArgusTokenReader;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;

@ApplicationScoped
@Authenticated
@Path("/auth/refresh")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RefreshAuthResource {

    private final ArgusTokenIssueRepository tokens;
    private final ArgusTokenReader reader;

    public RefreshAuthResource(ArgusTokenIssueRepository tokens,
                               ArgusTokenReader reader) {
        this.tokens = tokens;
        this.reader = reader;
    }

    @POST
    public Uni<RestResponse<ArgusToken>> auth() {
        // avoid allowing passing of access token
        if (reader.isAccessToken()) {
            throw new IllegalArgumentException("Access token cannot be used as a refresh token.");
        }
        // note: expiration time here is not important, but have to pass as is marked non-null
        var refreshToken = new ArgusToken(reader.rawToken(), Instant.now());
        return tokens.fromRefresh(refreshToken, reader.namespaceUser())
                .map(RestResponse::ok);
    }
}
