package dev.zygon.argus.auth.service;

import dev.zygon.argus.auth.MojangAuthStatus;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/hasJoined")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "mojang-auth-api")
public interface MojangAuthService {

    @GET
    Uni<MojangAuthStatus> authorize(@QueryParam("username") String username,
                                    @QueryParam("serverId") String hash);
}
