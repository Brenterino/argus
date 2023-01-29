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

import dev.zygon.argus.group.audit.AuditLog;
import dev.zygon.argus.group.auth.Authorizer;
import dev.zygon.argus.group.exception.GroupException;
import dev.zygon.argus.group.repository.AuditRepository;
import dev.zygon.argus.group.repository.PermissionRepository;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static dev.zygon.argus.group.mutiny.UniExtensions.failIfFalse;
import static dev.zygon.argus.permission.Permission.ADMIN;
import static org.jboss.resteasy.reactive.RestResponse.Status.BAD_REQUEST;
import static org.jboss.resteasy.reactive.RestResponse.Status.FORBIDDEN;

@ApplicationScoped
@Authenticated
@Path("/groups/audits/{groupName}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditGroupsResource {

    private final Authorizer authorizer;
    private final AuditRepository audits;
    private final PermissionRepository permissions;

    public AuditGroupsResource(Authorizer authorizer,
                               AuditRepository audits,
                               PermissionRepository permissions) {
        this.authorizer = authorizer;
        this.audits = audits;
        this.permissions = permissions;
    }

    @GET
    public Uni<RestResponse<AuditLog>> audit(@PathParam("groupName") String groupName,
                                             @DefaultValue("0") @QueryParam("page") int page,
                                             @DefaultValue("25") @QueryParam("size") int size) {
        var namespaceUser = authorizer.namespaceUser();
        var group = authorizer.group(groupName);
        return Uni.createFrom()
                .item(page >= 0 && size > 0)
                .plug(failIfFalse(new GroupException(BAD_REQUEST, "Invalid paging arguments provided.")))
                .replaceWith(permissions.hasPermission(group, namespaceUser, ADMIN))
                .plug(failIfFalse(new GroupException(FORBIDDEN, "You do not have permissions to view the audit log of this group.")))
                .replaceWith(audits.forGroup(group, page, size))
                .map(RestResponse::ok);
    }
}
