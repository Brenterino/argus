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
import dev.zygon.argus.group.auth.Authorizer;
import dev.zygon.argus.group.exception.GroupException;
import dev.zygon.argus.group.repository.AuditRepository;
import dev.zygon.argus.group.repository.GroupRepository;
import dev.zygon.argus.group.repository.PermissionRepository;
import dev.zygon.argus.permission.Permission;
import dev.zygon.argus.user.NamespaceUser;
import dev.zygon.argus.user.User;
import dev.zygon.argus.permission.UserPermission;
import dev.zygon.argus.permission.UserPermissions;
import io.quarkus.security.Authenticated;
import io.quarkus.vertx.web.Body;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.OffsetDateTime;

import static dev.zygon.argus.permission.Permission.ACCESS;
import static dev.zygon.argus.permission.Permission.ADMIN;
import static dev.zygon.argus.group.mutiny.UniExtensions.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.jboss.resteasy.reactive.RestResponse.Status.BAD_REQUEST;
import static org.jboss.resteasy.reactive.RestResponse.Status.CREATED;
import static org.jboss.resteasy.reactive.RestResponse.Status.NOT_FOUND;

@ApplicationScoped
@Authenticated
@Path("/groups/permissions/{groupName}/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminPermissionsResource {

    private final Authorizer authorizer;
    private final AuditRepository audits;
    private final GroupRepository groups;
    private final PermissionRepository permissions;

    public AdminPermissionsResource(Authorizer authorizer,
                                    AuditRepository audits,
                                    GroupRepository groups,
                                    PermissionRepository permissions) {
        this.authorizer = authorizer;
        this.audits = audits;
        this.groups = groups;
        this.permissions = permissions;
    }

    @GET
    public Uni<RestResponse<UserPermissions>> members(@PathParam("groupName") String groupName,
                                                      @DefaultValue("0") @QueryParam("page") int page,
                                                      @DefaultValue("25") @QueryParam("size") int size) {
        var namespaceUser = authorizer.namespaceUser();
        var group = authorizer.group(groupName);
        return Uni.createFrom()
                .item(page >= 0 && size > 0)
                .plug(failIfFalse(new GroupException(BAD_REQUEST, "Invalid paging arguments provided.")))
                .replaceWith(permissions.hasPermission(group, namespaceUser, ADMIN))
                .plug(failIfFalse(new GroupException(FORBIDDEN, "You do not have permissions to view members of this group.")))
                .replaceWith(permissions.forGroup(group, page, size))
                .map(RestResponse::ok);
    }

    @POST
    public Uni<RestResponse<Void>> invite(@PathParam("groupName") String groupName,
                                          @Body UserPermission target) {
        var namespaceUser = authorizer.namespaceUser();
        var group = authorizer.group(groupName);
        var targetNamespaceUser = authorizer.user(target.uuid());
        var permission = target.permission();
        var audit = createAudit(namespaceUser, targetNamespaceUser,
                permission, AuditAction.INVITE);
        return Uni.createFrom()
                .item(namespaceUser.user().equals(targetNamespaceUser.user()))
                .plug(failIfTrue(new GroupException(BAD_REQUEST, "You cannot invite yourself to a group.")))
                .replaceWith(permissions.hasPermission(group, namespaceUser, ADMIN))
                .plug(failIfFalse(new GroupException(FORBIDDEN, "You do not have permissions to invite members to this group.")))
                .replaceWith(permissions.hasPermission(group, targetNamespaceUser, ACCESS))
                .plug(failIfTrue(new GroupException(CONFLICT, "This user is already a member of this group.")))
                .replaceWith(ADMIN == permission)
                .plug(checkIfTrue(groups.ownedBy(group, namespaceUser)))
                .plug(failIfFalse(new GroupException(FORBIDDEN, "Only the owner of this group can invite users as admin.")))
                .replaceWith(permissions.grant(group, targetNamespaceUser, permission))
                .plug(failIfFalse(new GroupException("This user could not be invited to this group.")))
                .replaceWith(audits.create(group, audit))
                .plug(failIfFalse(new GroupException("User was invited, but no audit trail was left behind.")))
                .replaceWith(RestResponse.status(CREATED));
    }

    @PUT
    public Uni<RestResponse<Void>> modify(@PathParam("groupName") String groupName,
                                          @Body UserPermission target) {
        var namespaceUser = authorizer.namespaceUser();
        var group = authorizer.group(groupName);
        var targetNamespaceUser = authorizer.user(target.uuid());
        var permission = target.permission();
        var audit = createAudit(namespaceUser, targetNamespaceUser,
                permission, AuditAction.MODIFY);
        return Uni.createFrom()
                .item(namespaceUser.user().equals(targetNamespaceUser.user()))
                .plug(failIfTrue(new GroupException(BAD_REQUEST, "You cannot modify your own permissions.")))
                .replaceWith(permissions.hasPermission(group, namespaceUser, ADMIN))
                .plug(failIfFalse(new GroupException(FORBIDDEN, "You do not have permissions to modify members of this group.")))
                .replaceWith(permissions.hasPermission(group, targetNamespaceUser, ACCESS))
                .plug(failIfFalse(new GroupException(NOT_FOUND, "The user being modified is not a member of this group.")))
                .replaceWith(permission == ADMIN)
                .plug(checkIfFalse(permissions.hasPermission(group, targetNamespaceUser, ADMIN)))
                .plug(checkIfTrue(groups.ownedBy(group, namespaceUser)))
                .plug(failIfFalse(new GroupException(FORBIDDEN, "Only the owner of this group can promote or demote admins.")))
                .replaceWith(permissions.grant(group, targetNamespaceUser, permission))
                .plug(failIfFalse(new GroupException("Permissions could not be modified for this user.")))
                .replaceWith(audits.create(group, audit))
                .plug(failIfFalse(new GroupException("User was modified, but no audit trail was left behind.")))
                .replaceWith(RestResponse.status(NO_CONTENT));
    }

    @DELETE
    public Uni<RestResponse<Void>> kick(@PathParam("groupName") String groupName,
                                        @Body User target) {
        var namespaceUser = authorizer.namespaceUser();
        var group = authorizer.group(groupName);
        var targetNamespaceUser = authorizer.user(target.uuid());
        var audit = createAudit(namespaceUser, targetNamespaceUser,
                ACCESS, AuditAction.KICK);
        return Uni.createFrom()
                .item(namespaceUser.user().equals(targetNamespaceUser.user()))
                .plug(failIfTrue(new GroupException(BAD_REQUEST, "You cannot kick yourself from a group.")))
                .replaceWith(permissions.hasPermission(group, namespaceUser, ADMIN))
                .plug(failIfFalse(new GroupException(FORBIDDEN, "You do not have permissions to kick members from this group.")))
                .replaceWith(permissions.hasPermission(group, targetNamespaceUser, ACCESS))
                .plug(failIfFalse(new GroupException(NOT_FOUND, "The user being kicked is not a member of this group.")))
                .replaceWith(permissions.hasPermission(group, targetNamespaceUser, ADMIN))
                .plug(checkIfTrue(groups.ownedBy(group, namespaceUser)))
                .plug(failIfFalse(new GroupException(FORBIDDEN, "Only the owner of this group can kick admins.")))
                .replaceWith(permissions.remove(group, targetNamespaceUser))
                .plug(failIfFalse(new GroupException("Permissions could not be modified for this user.")))
                .replaceWith(audits.create(group, audit))
                .plug(failIfFalse(new GroupException("User was kicked, but no audit trail was left behind.")))
                .replaceWith(RestResponse.status(NO_CONTENT));
    }

    private Audit createAudit(NamespaceUser changeUser,
                              NamespaceUser targetUser,
                              Permission permission,
                              AuditAction action) {
        var changer = changeUser.user();
        var target = targetUser.user();
        return Audit.builder()
                .changer(changer.uuid())
                .target(target.uuid())
                .action(action)
                .permission(permission)
                .occurred(OffsetDateTime.now())
                .build();
    }
}
