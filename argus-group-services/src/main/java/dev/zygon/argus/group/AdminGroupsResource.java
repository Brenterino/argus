package dev.zygon.argus.group;

import dev.zygon.argus.group.audit.Audit;
import dev.zygon.argus.group.audit.AuditAction;
import dev.zygon.argus.group.auth.Authorizer;
import dev.zygon.argus.group.configuration.GroupConfiguration;
import dev.zygon.argus.group.exception.GroupException;
import dev.zygon.argus.group.repository.AuditRepository;
import dev.zygon.argus.group.repository.GroupRepository;
import dev.zygon.argus.group.repository.PermissionRepository;
import dev.zygon.argus.user.NamespaceUser;
import io.quarkus.security.Authenticated;
import io.quarkus.vertx.web.Body;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import static dev.zygon.argus.group.audit.AuditAction.CREATE;
import static dev.zygon.argus.group.audit.AuditAction.UPDATE;
import static dev.zygon.argus.permission.Permission.ADMIN;
import static dev.zygon.argus.group.mutiny.UniExtensions.failIfFalse;
import static dev.zygon.argus.group.mutiny.UniExtensions.failIfTrue;
import static javax.ws.rs.core.Response.Status.*;

@ApplicationScoped
@Authenticated
@Path("/groups/{groupName}/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminGroupsResource {

    private final Authorizer authorizer;
    private final AuditRepository audits;
    private final GroupRepository groups;
    private final PermissionRepository permissions;
    private final GroupConfiguration configuration;

    public AdminGroupsResource(Authorizer authorizer,
                               AuditRepository audits,
                               GroupRepository groups,
                               PermissionRepository permissions,
                               GroupConfiguration configuration) {
        this.authorizer = authorizer;
        this.audits = audits;
        this.groups = groups;
        this.permissions = permissions;
        this.configuration = configuration;
    }

    @POST
    public Uni<RestResponse<Void>> create(@PathParam("groupName") String groupName,
                                          @Body Map<String, Object> metadata) {
        var namespaceUser = authorizer.namespaceUser();
        var group = authorizer.group(groupName, metadata);
        var audit = createAudit(namespaceUser, CREATE);
        return groups.ownedBy(namespaceUser)
                .map(Groups::groups)
                .map(Set::size)
                .map(count -> count < configuration.maxOwnedGroups())
                .plug(failIfFalse(new GroupException(FORBIDDEN, "You are not allowed to create any more groups.")))
                .replaceWith(groups.exists(group))
                .plug(failIfTrue(new GroupException(CONFLICT, "This group already exists.")))
                .replaceWith(groups.create(group, namespaceUser))
                .plug(failIfFalse(new GroupException("This group could not be created.")))
                .replaceWith(audits.create(group, audit))
                .plug(failIfFalse(new GroupException("Group was created, but no audit trail was left behind.")))
                .replaceWith(RestResponse.status(CREATED));
    }

    @PUT
    public Uni<RestResponse<Void>> update(@PathParam("groupName") String groupName,
                                          @Body Map<String, Object> metadata) {
        var namespaceUser = authorizer.namespaceUser();
        var group = authorizer.group(groupName, metadata);
        var audit = createAudit(namespaceUser, UPDATE);
        return permissions.hasPermission(group, namespaceUser, ADMIN)
                .plug(failIfFalse(new GroupException(FORBIDDEN, "You do not have permissions to modify this group.")))
                .replaceWith(groups.update(group))
                .plug(failIfFalse(new GroupException("This group could not be updated.")))
                .replaceWith(audits.create(group, audit))
                .plug(failIfFalse(new GroupException("Group was updated, but no audit trail was left behind.")))
                .replaceWith(RestResponse.status(NO_CONTENT));
    }

    @DELETE
    public Uni<RestResponse<Void>> delete(@PathParam("groupName") String groupName) {
        var namespaceUser = authorizer.namespaceUser();
        var group = authorizer.group(groupName);
        return groups.ownedBy(group, namespaceUser)
                .plug(failIfFalse(new GroupException(FORBIDDEN, "You do not have permissions to delete this group.")))
                .replaceWith(audits.delete(group))
                .plug(failIfFalse(new GroupException("Could not delete audit data for this group. Cannot delete group.")))
                .replaceWith(groups.delete(group))
                .plug(failIfFalse(new GroupException("This group could not be deleted.")))
                .replaceWith(RestResponse.status(NO_CONTENT));
    }

    private static Audit createAudit(NamespaceUser namespaceUser, AuditAction action) {
        var user = namespaceUser.user();
        return Audit.builder()
                .changer(user.uuid())
                .target(user.uuid())
                .action(action)
                .permission(ADMIN)
                .occurred(OffsetDateTime.now())
                .build();
    }
}
