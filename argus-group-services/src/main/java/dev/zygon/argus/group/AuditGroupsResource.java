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
@Path("/audits/{groupName}")
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
