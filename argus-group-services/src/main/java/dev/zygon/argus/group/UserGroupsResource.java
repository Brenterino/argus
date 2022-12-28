package dev.zygon.argus.group;

import dev.zygon.argus.group.auth.Authorizer;
import dev.zygon.argus.group.exception.GroupException;
import dev.zygon.argus.group.mutiny.UniExtensions;
import dev.zygon.argus.group.repository.GroupRepository;
import dev.zygon.argus.group.repository.PermissionRepository;
import dev.zygon.argus.permission.GroupPermissions;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import lombok.experimental.ExtensionMethod;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static dev.zygon.argus.permission.Permission.ACCESS;
import static dev.zygon.argus.group.mutiny.UniExtensions.failIfFalse;
import static dev.zygon.argus.group.mutiny.UniExtensions.failIfTrue;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@ApplicationScoped
@Authenticated
@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ExtensionMethod(UniExtensions.class)
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
