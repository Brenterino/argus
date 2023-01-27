package dev.zygon.argus.auth;

import dev.zygon.argus.auth.repository.ArgusTokenIssueRepository;
import dev.zygon.argus.auth.repository.MojangAuthRepository;
import io.quarkus.vertx.web.Body;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/auth/mojang")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MojangAuthResource {

    private final ArgusTokenIssueRepository argus;
    private final MojangAuthRepository mojang;

    public MojangAuthResource(ArgusTokenIssueRepository argus,
                              MojangAuthRepository mojang) {
        this.argus = argus;
        this.mojang = mojang;
    }

    @POST
    public Uni<RestResponse<ArgusToken>> auth(@Body MojangAuthData authData) {
        return mojang.status(authData)
                .flatMap(authStatus -> argus.issue(authData, authStatus))
                .map(RestResponse::ok);
    }
}
