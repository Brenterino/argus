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
package dev.zygon.argus.auth.service;

import dev.zygon.argus.namespace.Namespace;
import dev.zygon.argus.permission.GroupPermissions;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "argus-groups-api")
public interface ArgusGroupService {

    @GET
    @Path("/groups/permissions")
    Uni<GroupPermissions> elected(@HeaderParam("Authorization") String token);

    @GET
    @Path("/groups/namespaces")
    Uni<Namespace> namespace(@QueryParam("mapping") String mapping);
}
