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
import dev.zygon.argus.auth.repository.MojangAuthRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
@Path("/auth/mojang")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MojangAuthResource {

    private final ArgusTokenIssueRepository tokens;
    private final MojangAuthRepository mojang;

    public MojangAuthResource(ArgusTokenIssueRepository tokens,
                              MojangAuthRepository mojang) {
        this.tokens = tokens;
        this.mojang = mojang;
    }

    @POST
    public Uni<RestResponse<DualToken>> auth(MojangAuthData authData) {
        return mojang.status(authData)
                .flatMap(authStatus -> tokens.fromMojang(authData, authStatus))
                .map(RestResponse::ok);
    }
}
