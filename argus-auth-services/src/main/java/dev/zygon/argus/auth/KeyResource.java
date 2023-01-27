package dev.zygon.argus.auth;

import dev.zygon.argus.auth.repository.KeyRepository;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.file.AsyncFile;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/auth/key")
public class KeyResource {

    private final KeyRepository keys;

    public KeyResource(KeyRepository keys) {
        this.keys = keys;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<RestResponse<AsyncFile>> publicKey() {
        return keys.publicKey()
                .map(RestResponse::ok);
    }
}
