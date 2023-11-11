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

import dev.zygon.argus.auth.repository.ArgusBannedUserRepository;
import dev.zygon.argus.user.User;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.jooq.generated.Tables.BANNED_USERS;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.using;

@Slf4j
@ApplicationScoped
public class ArgusBannedUserReactiveJooqRepository implements ArgusBannedUserRepository {

    private final Pool pool;
    private final Configuration configuration;
    private final Map<String, String> queryCache;

    public ArgusBannedUserReactiveJooqRepository(Pool pool, Configuration configuration) {
        this.pool = pool;
        this.configuration = configuration;
        this.queryCache = new ConcurrentHashMap<>();
    }

    @Override
    public Uni<Boolean> isUserBanned(User user) {
        return isUserBanned(user.uuid());
    }

    @Override
    public Uni<Boolean> isUserBanned(UUID uuid) {
        final var USER_BANNED = "USER_BANNED";
        final var COUNT_NAME = "count";
        var userBannedSql = queryCache.computeIfAbsent(USER_BANNED,
                k -> renderUserBannedSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Is User Banned) SQL: {}", userBannedSql);
            log.debug("Operation(Is User Banned) Params: user({})", uuid);
        }
        return pool.preparedQuery(userBannedSql)
                .execute(Tuple.of(uuid))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> row.getInteger(COUNT_NAME) > 0)
                .onFailure()
                .transform(e -> new WebApplicationException("Determining if user is banned unexpectedly failed.", e));
    }

    private String renderUserBannedSql() {
        return using(configuration)
                .selectCount()
                .from(BANNED_USERS)
                .where(BANNED_USERS.UUID.eq(field("$1", UUID.class)))
                .getSQL();
    }
}
