package dev.zygon.argus.group.repository.impl;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.exception.FatalGroupException;
import dev.zygon.argus.group.repository.PermissionRepository;
import dev.zygon.argus.permission.*;
import dev.zygon.argus.user.NamespaceUser;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.zygon.argus.group.repository.impl.ColumnNames.*;
import static dev.zygon.argus.group.repository.impl.RowMappers.group;
import static dev.zygon.argus.group.repository.impl.RowMappers.permission;
import static org.jooq.generated.Tables.*;
import static org.jooq.impl.DSL.*;

@Slf4j
@ApplicationScoped
public class PermissionReactiveJooqRepository implements PermissionRepository {

    private final Pool pool;
    private final Configuration configuration;
    private final Map<String, String> queryCache;

    public PermissionReactiveJooqRepository(Pool pool, Configuration configuration) {
        this.pool = pool;
        this.configuration = configuration;
        this.queryCache = new HashMap<>();
    }

    @Override
    public Uni<UserPermissions> forGroup(Group group) {
        final var FOR_GROUP = "FOR_GROUP";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var forGroupSql = queryCache.computeIfAbsent(FOR_GROUP,
                k -> renderForGroupSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Permissions for Group) SQL: {}", forGroupSql);
            log.debug("Operation(Permissions for Group) Params: namespace({}), group({})",
                    namespace, name);
        }
        return pool.preparedQuery(forGroupSql)
                .execute(Tuple.of(namespace, name))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .map(row -> new UserPermission(RowMappers.uuid(row), permission(row)))
                .collect().with(Collectors.toUnmodifiableSet())
                .map(UserPermissions::new)
                .onFailure()
                .transform(e -> new FatalGroupException("Retrieving permissions for group unexpectedly failed.", e));
    }

    private String renderForGroupSql() {
        return using(configuration)
                .select(
                        USER_PERMISSIONS.UUID.as(USER_UUID_NAME),
                        USER_PERMISSIONS.PERMISSION.as(PERMISSION_NAME)
                ).from(USER_PERMISSIONS)
                .where(USER_PERMISSIONS.GROUP_ID.eq(groupSelect()))
                .getSQL();
    }

    @Override
    public Uni<Boolean> grant(Group group, NamespaceUser namespaceUser, Permission permission) {
        final var GRANT_ACCESS = "GRANT_ACCESS";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var user = namespaceUser.user();
        var uuid = user.uuid();
        var grantSql = queryCache.computeIfAbsent(GRANT_ACCESS,
                k -> renderGrantSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Grant Access) SQL: {}", grantSql);
            log.debug("Operation(Grant Access) Params: namespace({}), group({}), uuid({}), permission({})",
                    namespace, name, uuid, permission);
        }
        return pool.preparedQuery(grantSql)
                .execute(Tuple.of(namespace, name, uuid, permission.ordinal()))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> true)
                .onFailure()
                .transform(e -> new FatalGroupException("Granting user permission for group unexpectedly failed.", e));
    }

    private String renderGrantSql() {
        return using(configuration)
                .insertInto(USER_PERMISSIONS,
                        USER_PERMISSIONS.GROUP_ID, USER_PERMISSIONS.UUID, USER_PERMISSIONS.PERMISSION, USER_PERMISSIONS.ELECTED
                )
                .values(field(groupSelect()), field("$3", UUID.class),
                        field("$4", Integer.class), defaultValue(Integer.class))
                .onDuplicateKeyUpdate()
                .set(USER_PERMISSIONS.PERMISSION, field("$4", Integer.class))
                .set(USER_PERMISSIONS.ELECTED,
                        when(USER_PERMISSIONS.ELECTED.gt(field("$4", Integer.class)),
                                field("$4", Integer.class))
                                .otherwise(USER_PERMISSIONS.ELECTED)
                ).getSQL();
    }

    @Override
    public Uni<Boolean> remove(Group group, NamespaceUser namespaceUser) {
        final var REMOVE_GROUP = "REMOVE_PERMISSION";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var user = namespaceUser.user();
        var uuid = user.uuid();
        var removePermissionsSql = queryCache.computeIfAbsent(REMOVE_GROUP,
                k -> renderRemovePermissionSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Remove Permission For Namespace User) SQL: {}", removePermissionsSql);
            log.debug("Operation(Remove Permission For Namespace User) Params: namespace({}), group({}), user({})",
                    namespace, name, uuid);
        }
        return pool.preparedQuery(removePermissionsSql)
                .execute(Tuple.of(namespace, name, uuid))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> true)
                .onFailure()
                .transform(e -> new FatalGroupException("Removing user permission for group unexpectedly failed.", e));
    }

    private String renderRemovePermissionSql() {
        return using(configuration)
                .deleteFrom(USER_PERMISSIONS)
                .where(USER_PERMISSIONS.GROUP_ID.eq(groupSelect()))
                .and(USER_PERMISSIONS.UUID.eq(field("$3", UUID.class)))
                .getSQL();
    }

    @Override
    public Uni<Boolean> hasPermission(Group group, NamespaceUser namespaceUser, Permission permission) {
        final var HAS_PERMISSION_QUERY = "HAS_PERMISSION";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var user = namespaceUser.user();
        var uuid = user.uuid();
        var hasPermissionSql = queryCache.computeIfAbsent(HAS_PERMISSION_QUERY,
                k -> renderHasPermissionSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Has Permission) SQL: {}", hasPermissionSql);
            log.debug("Operation(Has Permission) Params: namespace({}), group({}), user({}), permission({})",
                    namespace, name, uuid, permission);
        }
        return pool.preparedQuery(hasPermissionSql)
                .execute(Tuple.of(namespace, name, uuid, permission.ordinal()))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> row.getInteger(COUNT_NAME) > 0)
                .onFailure()
                .transform(e -> new FatalGroupException("Determining if user has permission unexpectedly failed.", e));
    }

    private String renderHasPermissionSql() {
        return using(configuration)
                .selectCount()
                .from(USER_PERMISSIONS)
                .where(USER_PERMISSIONS.GROUP_ID.eq(groupSelect()))
                .and(USER_PERMISSIONS.UUID.eq(field("$3", UUID.class)))
                .and(USER_PERMISSIONS.PERMISSION.ge(field("$4", Integer.class)))
                .getSQL();
    }

    @Override
    public Uni<GroupPermissions> available(NamespaceUser namespaceUser) {
        final var PERMISSIONS_QUERY = "PERMISSIONS";
        var groupsByNamespaceUserSql = queryCache.computeIfAbsent(PERMISSIONS_QUERY,
                k -> renderPermissionsSql());
        return retrievePermissions(namespaceUser, groupsByNamespaceUserSql);
    }

    private String renderPermissionsSql() {
        return using(configuration)
                .select(
                        NAMESPACES.NAME.as(NAMESPACE_NAME),
                        GROUPS.NAME.as(GROUP_NAME),
                        USER_PERMISSIONS.PERMISSION.as(PERMISSION_NAME),
                        GROUPS.METADATA
                )
                .from(USER_PERMISSIONS, GROUPS, NAMESPACES)
                .where(NAMESPACES.NAME.eq(field("$1", String.class)))
                .and(USER_PERMISSIONS.UUID.eq(field("$2", UUID.class)))
                .and(USER_PERMISSIONS.GROUP_ID.eq(GROUPS.ID))
                .and(GROUPS.NAMESPACE_ID.eq(NAMESPACES.ID))
                .getSQL();
    }

    @Override
    public Uni<GroupPermissions> elected(NamespaceUser namespaceUser) {
        final var ELECTED_QUERY = "ELECTED";
        var electedByUserSql = queryCache.computeIfAbsent(ELECTED_QUERY,
                k -> renderElectedSql());
        return retrievePermissions(namespaceUser, electedByUserSql);
    }

    private String renderElectedSql() {
        return using(configuration)
                .select(
                        NAMESPACES.NAME.as(NAMESPACE_NAME),
                        GROUPS.NAME.as(GROUP_NAME),
                        USER_PERMISSIONS.ELECTED.as(PERMISSION_NAME)
                )
                .from(USER_PERMISSIONS, GROUPS, NAMESPACES)
                .where(NAMESPACES.NAME.eq(field("$1", String.class)))
                .and(USER_PERMISSIONS.UUID.eq(field("$2", UUID.class)))
                .and(USER_PERMISSIONS.GROUP_ID.eq(GROUPS.ID))
                .and(GROUPS.NAMESPACE_ID.eq(NAMESPACES.ID))
                .getSQL();
    }

    private Uni<GroupPermissions> retrievePermissions(NamespaceUser namespaceUser, String sql) {
        var namespace = namespaceUser.namespace();
        var user = namespaceUser.user();
        var uuid = user.uuid();
        if (log.isDebugEnabled()) {
            log.debug("Operation(Retrieve Permission By Namespace User) SQL: {}", sql);
            log.debug("Operation(Retrieve Permission By Namespace User) Params: namespace({}), user({})",
                    namespace, uuid);
        }
        return pool.preparedQuery(sql)
                .execute(Tuple.of(namespace, uuid))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .map(row -> new GroupPermission(group(row), permission(row)))
                .collect().with(Collectors.toUnmodifiableSet())
                .map(GroupPermissions::new)
                .onFailure()
                .transform(e -> new FatalGroupException("Finding groups for user unexpectedly failed.", e));
    }

    @Override
    public Uni<Boolean> elect(Group group, NamespaceUser namespaceUser, Permission permission) {
        final var ELECT_QUERY = "ELECT";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var user = namespaceUser.user();
        var uuid = user.uuid();
        var electQuery = queryCache.computeIfAbsent(ELECT_QUERY,
                k -> renderElectSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Elect Permission For Namespace User) SQL: {}", electQuery);
            log.debug("Operation(Elect Permission For Namespace User) Params: namespace({}), group({}), user({}), permission({})",
                    namespace, name, uuid, permission);
        }
        return pool.preparedQuery(electQuery)
                .execute(Tuple.of(namespace, name, uuid, permission.ordinal()))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> true)
                .onFailure()
                .transform(e -> new FatalGroupException("Electing user permission for group unexpectedly failed.", e));
    }

    private String renderElectSql() {
        return using(configuration)
                .update(USER_PERMISSIONS)
                .set(USER_PERMISSIONS.ELECTED, field("$4", Integer.class))
                .where(USER_PERMISSIONS.GROUP_ID.eq(groupSelect()))
                .and(USER_PERMISSIONS.UUID.eq(field("$3", UUID.class)))
                .getSQL();
    }

    private SelectConditionStep<Record1<Long>> groupSelect() {
        return using(configuration)
                .select(GROUPS.ID)
                .from(GROUPS)
                .where(GROUPS.NAMESPACE_ID.eq(namespaceSelect()))
                .and(lower(GROUPS.NAME).eq(field("$2", String.class)));
    }

    private SelectConditionStep<Record1<Long>> namespaceSelect() {
        return using(configuration)
                .select(NAMESPACES.ID)
                .from(NAMESPACES)
                .where(NAMESPACES.NAME.eq(field("$1", String.class)));
    }
}
