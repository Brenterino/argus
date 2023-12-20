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
package dev.zygon.argus.auth.repository;

import dev.zygon.argus.auth.OneTimePassword;
import dev.zygon.argus.auth.configuration.AuthConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static dev.zygon.argus.mutiny.UniExtensions.failIfFalse;
import static org.jooq.generated.Keys.ONE_TIME_PASSWORDS_USER_UNIQUE;
import static org.jooq.generated.Tables.ONE_TIME_PASSWORDS;
import static org.jooq.impl.DSL.*;

@Slf4j
@ApplicationScoped
public class ArgusOneTimePasswordReactiveJooqRepository implements ArgusOneTimePasswordRepository {

    private final Pool pool;
    private final Configuration config;
    private final AuthConfiguration authConfig;
    private final Random otpRandom;
    private final Map<String, String> queryCache;

    public ArgusOneTimePasswordReactiveJooqRepository(Pool pool,
                                                      Configuration config,
                                                      AuthConfiguration authConfig) {
        this.pool = pool;
        this.config = config;
        this.authConfig = authConfig;
        this.otpRandom = new Random();
        this.queryCache = new ConcurrentHashMap<>();
    }

    @Override
    public String generate() {
        var authChars = authConfig.otpAllowedCharacters();
        return IntStream.range(0, authConfig.otpLength() - 1)
                .map(x -> otpRandom.nextInt(authChars.length()))
                .map(authChars::charAt)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    @Override
    public Uni<OneTimePassword> storePassword(OneTimePassword password) {
        return canCreatePassword(password)
                .plug(failIfFalse(new IllegalStateException("New One Time Password cannot be generated.")))
                .replaceWith(storePasswordInternal(password));
    }

    private Uni<Boolean> canCreatePassword(OneTimePassword password) {
        final var CAN_CREATE_PASSWORD = "CAN_CREATE_PASSWORD";
        final var COUNT_NAME = "count";
        var uuid = password.uuid();
        var reissueWindow = Instant.now()
                .minus(authConfig.otpReissueDelayMinutes(), ChronoUnit.MINUTES)
                .atOffset(ZoneOffset.UTC);
        var canCreatePasswordSql = queryCache.computeIfAbsent(CAN_CREATE_PASSWORD,
                k -> renderCanCreatePasswordSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Can Create Password) SQL: {}", canCreatePasswordSql);
            log.debug("Operation(Can Create Password) Params: user({}) expiration({})",
                    uuid, reissueWindow);
        }
        return pool.preparedQuery(canCreatePasswordSql)
                .execute(Tuple.of(uuid, reissueWindow))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> row.getInteger(COUNT_NAME) == 0)
                .onFailure()
                .transform(e -> new WebApplicationException("Checking if One Time Password can be created unexpectedly failed.", e));
    }

    private String renderCanCreatePasswordSql() {
        return using(config)
                .selectCount()
                .from(ONE_TIME_PASSWORDS)
                .where(ONE_TIME_PASSWORDS.UUID.eq(field("$1", UUID.class)))
                  .and(ONE_TIME_PASSWORDS.EXPIRATION.ge(field("$2", OffsetDateTime.class)))
                .getSQL();
    }

    private Uni<OneTimePassword> storePasswordInternal(OneTimePassword password) {
        final var STORE_PASSWORD = "STORE_PASSWORD";
        var uuid = password.uuid();
        var pass = password.password();
        var expiration = Instant.now()
                .plus(authConfig.otpExpirationTimeMinutes(), ChronoUnit.MINUTES)
                .atOffset(ZoneOffset.UTC);
        var storePasswordSql = queryCache.computeIfAbsent(STORE_PASSWORD,
                k -> renderStorePasswordSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Store Password) SQL: {}", storePasswordSql);
            log.debug("Operation(Store Password) Params: user({}), password({}), expiration({})",
                    uuid, pass, expiration);
        }
        return pool.preparedQuery(storePasswordSql)
                .execute(Tuple.of(uuid, pass, expiration))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> password)
                .onFailure()
                .transform(e -> new WebApplicationException("Storing One Time Password unexpectedly failed.", e));
    }

    private String renderStorePasswordSql() {
        return using(config)
                .insertInto(ONE_TIME_PASSWORDS,
                        ONE_TIME_PASSWORDS.UUID, ONE_TIME_PASSWORDS.PASS, ONE_TIME_PASSWORDS.EXPIRATION
                )
                .values(field("$1", UUID.class), field("$2", String.class), field("$3", OffsetDateTime.class))
                .onConflictOnConstraint(ONE_TIME_PASSWORDS_USER_UNIQUE)
                .doUpdate()
                .set(ONE_TIME_PASSWORDS.PASS, field("$2", String.class))
                .set(ONE_TIME_PASSWORDS.EXPIRATION, field("$3", OffsetDateTime.class))
                .getSQL();
    }

    @Override
    public Uni<Boolean> verifyPassword(OneTimePassword password) {
        final var VERIFY_PASSWORD = "VERIFY_PASSWORD";
        final var COUNT_NAME = "count";
        var uuid = password.uuid();
        var pass = password.password();
        var nonExpiredWindow = Instant.now()
                .minus(authConfig.otpExpirationTimeMinutes(), ChronoUnit.MINUTES)
                .atOffset(ZoneOffset.UTC);
        var verifyPasswordSql = queryCache.computeIfAbsent(VERIFY_PASSWORD,
                k -> renderVerifyPasswordSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Verify Password) SQL: {}", verifyPasswordSql);
            log.debug("Operation(Verify Password) Params: user({}), password({}), expiration({})",
                    uuid, pass, nonExpiredWindow);
        }
        return pool.preparedQuery(verifyPasswordSql)
                .execute(Tuple.of(uuid, pass, nonExpiredWindow))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> row.getInteger(COUNT_NAME) > 0)
                .onFailure()
                .transform(e -> new WebApplicationException("Checking if One Time Password is valid unexpectedly failed.", e));
    }

    private String renderVerifyPasswordSql() {
        return using(config)
                .selectCount()
                .from(ONE_TIME_PASSWORDS)
                .where(ONE_TIME_PASSWORDS.UUID.eq(field("$1", UUID.class)))
                .and(ONE_TIME_PASSWORDS.PASS.eq(field("$2", String.class)))
                .and(ONE_TIME_PASSWORDS.EXPIRATION.ge(field("$3", OffsetDateTime.class)))
                .getSQL();
    }

    @Override
    public Uni<Boolean> deletePassword(OneTimePassword password) {
        final var DELETE_PASSWORD = "DELETE_PASSWORD";
        var uuid = password.uuid();
        var deletePasswordSql = queryCache.computeIfAbsent(DELETE_PASSWORD,
                k -> renderDeletePasswordSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Delete Password) SQL: {}", deletePasswordSql);
            log.debug("Operation(Delete Password) Params: user({})", uuid);
        }
        return pool.preparedQuery(deletePasswordSql)
                .execute(Tuple.of(uuid))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> true)
                .onFailure()
                .transform(e -> new WebApplicationException("Deleting used one-time-password failed unexpectedly.", e));
    }

    private String renderDeletePasswordSql() {
        return using(config)
                .deleteFrom(ONE_TIME_PASSWORDS)
                .where(ONE_TIME_PASSWORDS.UUID.eq(field("$1", UUID.class)))
                .getSQL();
    }

    @Override
    public Uni<Boolean> deleteExpiredPasswords() {
        final var DELETE_EXPIRED_PASSWORDS = "DELETE_EXPIRED_PASSWORDS";
        var currentTime = Instant.now()
                .atOffset(ZoneOffset.UTC);
        var deleteExpiredPasswordsSql = queryCache.computeIfAbsent(DELETE_EXPIRED_PASSWORDS,
                k -> renderDeleteExpiredPasswordsSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Deleted Expired Passwords) SQL: {}", deleteExpiredPasswordsSql);
            log.debug("Operation(Deleted Expired Passwords) Params: now({})", currentTime);
        }
        return pool.preparedQuery(deleteExpiredPasswordsSql)
                .execute(Tuple.of(currentTime))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> true)
                .onFailure()
                .transform(e -> new WebApplicationException(""));
    }

    private String renderDeleteExpiredPasswordsSql() {
        return using(config)
                .deleteFrom(ONE_TIME_PASSWORDS)
                .where(ONE_TIME_PASSWORDS.EXPIRATION.le(field("$1", OffsetDateTime.class)))
                .getSQL();
    }
}
