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

import dev.zygon.argus.group.audit.Audit;
import dev.zygon.argus.group.audit.AuditAction;
import dev.zygon.argus.group.exception.GroupException;
import dev.zygon.argus.group.repository.AuditRepository;
import dev.zygon.argus.group.repository.PermissionRepository;
import dev.zygon.argus.permission.GroupPermissions;
import dev.zygon.argus.permission.Permission;
import dev.zygon.argus.permission.UserPermission;
import dev.zygon.argus.auth.Authorizer;
import dev.zygon.argus.user.NamespaceUser;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.OffsetDateTime;

import static dev.zygon.argus.mutiny.UniExtensions.failIfFalse;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static org.jboss.resteasy.reactive.RestResponse.Status.FORBIDDEN;

@ApplicationScoped
@Authenticated
@Path("/groups/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserPermissionsResource {

    private final Authorizer authorizer;
    private final AuditRepository audits;
    private final PermissionRepository permissions;

    public UserPermissionsResource(Authorizer authorizer,
                                   AuditRepository audits,
                                   PermissionRepository permissions) {
        this.authorizer = authorizer;
        this.audits = audits;
        this.permissions = permissions;
    }

    @GET
    public Uni<RestResponse<GroupPermissions>> elected() {
        var namespaceUser = authorizer.namespaceUser();
        return permissions.elected(namespaceUser)
                .map(RestResponse::ok);
    }

    @PUT
    @Path("/{groupName}")
    public Uni<RestResponse<Void>> elect(@PathParam("groupName") String groupName,
                                         UserPermission userPermission) {
        var namespaceUser = authorizer.namespaceUser();
        var group = authorizer.group(groupName);
        var permission = userPermission.permission();
        var audit = createElectAudit(namespaceUser, permission);
        return permissions.hasPermission(group, namespaceUser, permission)
                .plug(failIfFalse(new GroupException(FORBIDDEN, "You cannot elect a permission that you do not have access to.")))
                .replaceWith(permissions.elect(group, namespaceUser, permission))
                .plug(failIfFalse(new GroupException("Could not elect permission.")))
                .replaceWith(audits.create(group, audit))
                .plug(failIfFalse(new GroupException("Permission was elected, but no audit trail was left behind.")))
                .replaceWith(RestResponse.status(NO_CONTENT));
    }

    private Audit createElectAudit(NamespaceUser changeUser,
                                   Permission permission) {
        var changer = changeUser.user();
        return Audit.builder()
                .changer(changer.uuid())
                .target(changer.uuid())
                .action(AuditAction.ELECT)
                .permission(permission)
                .occurred(OffsetDateTime.now())
                .build();
    }
}
