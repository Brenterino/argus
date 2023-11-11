/*
    Argus - Suite of services aimed to enhance Minecraft Multiplayer
    Copyright (C) 2023 Zygon

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.zygon.argus.auth.repository.impl;

import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;

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
