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
import dev.zygon.argus.auth.service.ArgusOneTimePasswordService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
@Path("/auth/otp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OneTimePassAuthResource {

    private final ArgusTokenIssueRepository tokens;
    private final ArgusOneTimePasswordService passwords;
    private final Authorizer authorizer;

    public OneTimePassAuthResource(ArgusTokenIssueRepository tokens,
                                   ArgusOneTimePasswordService passwords,
                                   Authorizer authorizer) {
        this.tokens = tokens;
        this.passwords = passwords;
        this.authorizer = authorizer;
    }

    @Authenticated
    @GET
    public Uni<RestResponse<OneTimePassword>> generate() {
        if (authorizer.isAccessToken()) {
            throw new IllegalArgumentException("Access token cannot be used to generate a one time password.");
        }
        return passwords.generate(authorizer.namespaceUser())
                .map(RestResponse::ok);
    }

    @POST
    public Uni<RestResponse<DualToken>> auth(OneTimePassword password) {
        return passwords.verify(password)
                .flatMap(tokens::fromOneTimePass)
                .map(RestResponse::ok);
    }
}
