package dev.zygon.argus.group.repository.impl;

import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Pool;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;

public class PoolConfiguration {

    /**
     * Convert {@link PgPool} type into {@link Pool} type because it is not
     * possible to directly use {@link Pool} in order to invert control over
     * implementation so that changes can be minimized to this location only
     * if a different DB vendor is selected.
     * <p>
     * Ideally this would not be needed since {@link PgPool} is a subclass of
     * {@link Pool} and would assume the container would allow injection using
     * the parent class, but this is not the case. It might be possible to
     * dynamically find the bean via configuration, but this might be hacky and
     * will likely not work well if compiled to a native image - which may
     * require explicit registration anyways.
     * </p>
     *
     * @param pgPool the {@link PgPool} bean which will be coerced into the
     *               {@link Pool} type for the purpose of injection.
     * @return the input {@link PgPool} bean represented as a {@link Pool}.
     */
    @Produces
    @ApplicationScoped
    public Pool pool(PgPool pgPool) {
        return pgPool;
    }
}
