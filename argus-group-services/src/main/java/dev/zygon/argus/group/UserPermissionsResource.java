package dev.zygon.argus.group;

import dev.zygon.argus.group.auth.Authorizer;
import dev.zygon.argus.group.exception.GroupException;
import dev.zygon.argus.group.mutiny.UniExtensions;
import dev.zygon.argus.group.repository.PermissionRepository;
import dev.zygon.argus.permission.GroupPermissions;
import dev.zygon.argus.permission.UserPermission;
import io.quarkus.security.Authenticated;
import io.quarkus.vertx.web.Body;
import io.smallrye.mutiny.Uni;
import lombok.experimental.ExtensionMethod;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static dev.zygon.argus.group.mutiny.UniExtensions.failIfFalse;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.jboss.resteasy.reactive.RestResponse.Status.FORBIDDEN;

@ApplicationScoped
@Authenticated
@Path("/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ExtensionMethod(UniExtensions.class)
public class UserPermissionsResource {

    private final Authorizer authorizer;
    private final PermissionRepository permissions;

    public UserPermissionsResource(Authorizer authorizer,
                                   PermissionRepository permissions) {
        this.authorizer = authorizer;
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
                                         @Body UserPermission userPermission) {
        var namespaceUser = authorizer.namespaceUser();
        var group = authorizer.group(groupName);
        var permission = userPermission.permission();
        return permissions.hasPermission(group, namespaceUser, permission)
                .plug(failIfFalse(new GroupException(FORBIDDEN, "You cannot elect a permission that you do not have access to.")))
                .replaceWith(permissions.elect(group, namespaceUser, permission))
                .plug(failIfFalse(new GroupException("Could not elect permission.")))
                .replaceWith(RestResponse.status(NO_CONTENT));
    }
}
