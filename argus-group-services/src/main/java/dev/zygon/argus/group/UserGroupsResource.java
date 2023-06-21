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

import dev.zygon.argus.group.auth.Authorizer;
import dev.zygon.argus.group.exception.GroupException;
import dev.zygon.argus.group.repository.GroupRepository;
import dev.zygon.argus.group.repository.PermissionRepository;
import dev.zygon.argus.permission.GroupPermissions;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static dev.zygon.argus.group.mutiny.UniExtensions.failIfFalse;
import static dev.zygon.argus.group.mutiny.UniExtensions.failIfTrue;
import static dev.zygon.argus.permission.Permission.ACCESS;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@ApplicationScoped
@Authenticated
@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserGroupsResource {

    private final Authorizer authorizer;
    private final GroupRepository groups;
    private final PermissionRepository permissions;

    public UserGroupsResource(Authorizer authorizer,
                              GroupRepository groups,
                              PermissionRepository permissions) {
        this.authorizer = authorizer;
        this.groups = groups;
        this.permissions = permissions;
    }

    @GET
    public Uni<RestResponse<GroupPermissions>> groups() {
        var namespaceUser = authorizer.namespaceUser();
        return permissions.available(namespaceUser)
                .map(RestResponse::ok);
    }

    @DELETE
    @Path("/{groupName}")
    public Uni<RestResponse<Void>> leave(@PathParam("groupName") String groupName) {
        var namespaceUser = authorizer.namespaceUser();
        var group = authorizer.group(groupName);
        return permissions.hasPermission(group, namespaceUser, ACCESS)
                .plug(failIfFalse(new GroupException(FORBIDDEN, "You are not a member of this group.")))
                .replaceWith(groups.ownedBy(group, namespaceUser))
                .plug(failIfTrue(new GroupException(FORBIDDEN, "You cannot leave a group you are the owner of.")))
                .replaceWith(permissions.remove(group, namespaceUser))
                .plug(failIfFalse(new GroupException("Could not leave group.")))
                .replaceWith(RestResponse.status(NO_CONTENT));
    }
}
