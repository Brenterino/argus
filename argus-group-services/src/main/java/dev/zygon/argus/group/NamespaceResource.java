package dev.zygon.argus.group;

import dev.zygon.argus.group.exception.GroupException;
import dev.zygon.argus.group.mutiny.UniExtensions;
import dev.zygon.argus.group.repository.NamespaceRepository;
import dev.zygon.argus.namespace.Namespace;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import lombok.experimental.ExtensionMethod;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static org.jboss.resteasy.reactive.RestResponse.Status.NOT_FOUND;

@ApplicationScoped
@Authenticated
@Path("/namespaces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ExtensionMethod(UniExtensions.class)
public class NamespaceResource {

    private final NamespaceRepository namespaces;

    public NamespaceResource(NamespaceRepository namespaces) {
        this.namespaces = namespaces;
    }

    @GET
    public Uni<RestResponse<Namespace>> matching(@QueryParam("mapping") String mapping) {
        return namespaces.matching(mapping)
                .map(result -> result
                        .orElseThrow(() -> new GroupException(NOT_FOUND, "Namespace mapping was not found.")))
                .map(RestResponse::ok);
    }
}
