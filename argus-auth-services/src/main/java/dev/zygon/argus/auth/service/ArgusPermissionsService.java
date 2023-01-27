package dev.zygon.argus.auth.service;

import dev.zygon.argus.permission.GroupPermissions;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/permissions")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "argus-permissions-api")
public interface ArgusPermissionsService {

    Uni<GroupPermissions> elected(@HeaderParam("Authorization") String token);
}
