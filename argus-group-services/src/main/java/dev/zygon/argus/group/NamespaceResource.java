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
package dev.zygon.argus.group;

import dev.zygon.argus.group.exception.GroupException;
import dev.zygon.argus.group.repository.NamespaceRepository;
import dev.zygon.argus.namespace.Namespace;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static org.jboss.resteasy.reactive.RestResponse.Status.NOT_FOUND;

@ApplicationScoped
@Path("/groups/namespaces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NamespaceResource {

    private final NamespaceRepository namespaces;

    public NamespaceResource(NamespaceRepository namespaces) {
        this.namespaces = namespaces;
    }

    @GET
    public Uni<RestResponse<Namespace>> matching(@QueryParam("mapping") String mapping) {
        return namespaces.matching(mapping)
                .map(result -> result
                        .orElseThrow(() -> new GroupException(NOT_FOUND, "Namespace mapping was not found.")))
                .map(RestResponse::ok);
    }
}
