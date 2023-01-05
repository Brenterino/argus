package dev.zygon.argus.group.repository.impl;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.Groups;
import dev.zygon.argus.group.exception.FatalGroupException;
import dev.zygon.argus.group.exception.GroupException;
import dev.zygon.argus.group.repository.GroupRepository;
import dev.zygon.argus.user.NamespaceUser;
import dev.zygon.argus.user.User;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;
import org.jooq.JSON;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.zygon.argus.group.repository.impl.ColumnNames.*;
import static dev.zygon.argus.group.repository.impl.CommonJooqRenderer.groupSelect;
import static dev.zygon.argus.group.repository.impl.CommonJooqRenderer.namespaceSelect;
import static dev.zygon.argus.permission.Permission.ADMIN;
import static org.jooq.generated.Tables.*;
import static org.jooq.impl.DSL.*;

@Slf4j
@ApplicationScoped
public class GroupReactiveJooqRepository implements GroupRepository {

    private final Pool pool;
    private final Configuration configuration;
    private final Map<String, String> queryCache;

    public GroupReactiveJooqRepository(Pool pool, Configuration configuration) {
        this.pool = pool;
        this.configuration = configuration;
        this.queryCache = new HashMap<>();
    }

    @Override
    public Uni<Groups> ownedBy(NamespaceUser namespaceUser) throws GroupException {
        final var GROUPS_OWNED_BY_QUERY = "GROUPS_OWNED_BY";
        var namespace = namespaceUser.namespace();
        var user = namespaceUser.user();
        var owner = user.uuid();
        var groupsOwnedBySql = queryCache.computeIfAbsent(GROUPS_OWNED_BY_QUERY,
                k -> renderGroupsOwnedSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Groups Owned By) SQL: {}", groupsOwnedBySql);
            log.debug("Operation(Groups Owned By) Params: namespace({}), owner({})",
                    namespace, owner);
        }
        return pool.preparedQuery(groupsOwnedBySql)
                .execute(Tuple.of(namespace, owner))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .map(RowMappers::group)
                .collect().with(Collectors.toUnmodifiableSet())
                .map(Groups::new)
                .onFailure()
                .transform(e -> new FatalGroupException("Determining groups owned unexpectedly failed.", e));
    }

    private String renderGroupsOwnedSql() {
        return using(configuration)
                .select(
                        NAMESPACES.NAME.as(NAMESPACE_NAME),
                        GROUPS.NAME.as(GROUP_NAME),
                        GROUPS.METADATA
                )
                .from(GROUPS, NAMESPACES)
                .where(NAMESPACES.NAME.eq(field("$1", String.class)))
                .and(GROUPS.OWNER.eq(field("$2", UUID.class)))
                .and(GROUPS.NAMESPACE_ID.eq(NAMESPACES.ID))
                .getSQL();
    }

    @Override
    public Uni<Boolean> ownedBy(Group group, NamespaceUser namespaceUser) throws GroupException {
        final var IS_GROUP_OWNED_BY_QUERY = "IS_GROUP_OWNED_BY";
        var namespace = namespaceUser.namespace();
        var name = group.name().toLowerCase();
        var user = namespaceUser.user();
        var owner = user.uuid();
        var isGroupOwnedBySql = queryCache.computeIfAbsent(IS_GROUP_OWNED_BY_QUERY,
                k -> renderIsGroupOwnedBy());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Is Group Owned By) SQL: {}", isGroupOwnedBySql);
            log.debug("Operation(Is Group Owned By) Params: namespace({}), group({}) owner({})",
                    namespace, name, owner);
        }
        return pool.preparedQuery(isGroupOwnedBySql)
                .execute(Tuple.of(namespace, name, owner))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> row.getInteger(COUNT_NAME) > 0)
                .onFailure()
                .transform(e -> new FatalGroupException("Determining groups owned unexpectedly failed.", e));
    }

    private String renderIsGroupOwnedBy() {
        return using(configuration)
                .selectCount()
                .from(GROUPS, NAMESPACES)
                .where(NAMESPACES.NAME.eq(field("$1", String.class)))
                .and(lower(GROUPS.NAME).eq(field("$2", String.class)))
                .and(GROUPS.OWNER.eq(field("$3", UUID.class)))
                .and(GROUPS.NAMESPACE_ID.eq(NAMESPACES.ID))
                .getSQL();
    }

    @Override
    public Uni<Boolean> exists(Group group) throws GroupException {
        final var GROUP_EXISTS_QUERY = "GROUP_EXISTS";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var groupExistsSql = queryCache.computeIfAbsent(GROUP_EXISTS_QUERY,
                k -> renderGroupExistsSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Group Exists) SQL: {}", groupExistsSql);
            log.debug("Operation(Group Exists) Params: namespace({}), name({})",
                    namespace, name);
        }
        return pool.preparedQuery(groupExistsSql)
                .execute(Tuple.of(namespace, name))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .map(row -> row.getLong(COUNT_NAME))
                .map(count -> count > 0)
                .collect().first()
                .onFailure()
                .transform(e -> new FatalGroupException("Determining group existence unexpectedly failed.", e));
    }

    private String renderGroupExistsSql() {
        return using(configuration)
                .selectCount()
                .from(GROUPS, NAMESPACES)
                .where(NAMESPACES.NAME.eq(field("$1", String.class)))
                .and(lower(GROUPS.NAME).eq(field("$2", String.class)))
                .and(GROUPS.NAMESPACE_ID.eq(NAMESPACES.ID))
                .getSQL();
    }

    @Override
    public Uni<Boolean> create(Group group, NamespaceUser user) throws GroupException {
        var creator = user.user();
        // Ideally, group creation would be 'pure' insofar as only creating a group,
        // but we are also assigning the admin role to the creator at the same time
        // to simplify certain paths for altering permissions
        return pool.withTransaction(client -> createGroup(client, creator, group)
                .chain(id -> createAdminRole(client, creator, id)));
    }

    private Uni<Long> createGroup(SqlConnection client, User creator, Group group) {
        final var CREATE_GROUP_QUERY = "CREATE_GROUP";
        var namespace = group.namespace();
        var name = group.name();
        var owner = creator.uuid();
        var metadata = group.metadata();
        var metadataJson = new JsonObject(metadata);
        var createGroupSql = queryCache.computeIfAbsent(CREATE_GROUP_QUERY,
                k -> renderCreateGroupSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Group Create) Create Group SQL: {}", createGroupSql);
            log.debug("Operation(Group Create) Create Group Params: namespace({}), name({}), owner({}), metadata({})",
                    namespace, name, owner, metadata);
        }
        return client.preparedQuery(createGroupSql)
                .execute(Tuple.of(namespace, name, owner)
                        .addJsonObject(metadataJson))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> row.getLong(ID_NAME))
                .onFailure()
                .transform(e -> new FatalGroupException("Group creation unexpectedly failed.", e));
    }

    private String renderCreateGroupSql() {
        return using(configuration)
                .insertInto(GROUPS,
                        GROUPS.NAMESPACE_ID, GROUPS.NAME, GROUPS.OWNER, GROUPS.METADATA)
                .values(field(namespaceSelect(configuration)), field("$2", String.class),
                        field("$3", UUID.class), field("$4", JSON.class))
                .returning(GROUPS.ID)
                .getSQL();
    }

    private Uni<Boolean> createAdminRole(SqlConnection client, User creator, Long groupId) {
        final var ADMIN_ROLE_QUERY = "ADMIN_ROLE";
        var owner = creator.uuid();
        var adminPermission = ADMIN.ordinal();
        var adminRoleSql = queryCache.computeIfAbsent(ADMIN_ROLE_QUERY,
                k -> renderAdminRoleSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Group Create) Admin Role SQL: {}", adminRoleSql);
            log.debug("Operation(Group Create) Admin Role Params: group({}), owner({}), permission({})",
                    groupId, owner, adminPermission);
        }
        return client.preparedQuery(adminRoleSql)
                .execute(Tuple.of(groupId, owner, adminPermission))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> true)
                .onFailure()
                .transform(e -> new FatalGroupException("Assigning admin permission to creator unexpectedly failed.", e));
    }

    private String renderAdminRoleSql() {
        return using(configuration)
                .insertInto(USER_PERMISSIONS,
                        USER_PERMISSIONS.GROUP_ID, USER_PERMISSIONS.UUID, USER_PERMISSIONS.PERMISSION, USER_PERMISSIONS.ELECTED
                )
                .values(field("$1", Long.class), field("$2", UUID.class),
                        field("$3", Integer.class), defaultValue(Integer.class))
                .getSQL();
    }

    @Override
    public Uni<Boolean> update(Group group) throws GroupException {
        final var UPDATE_GROUP_QUERY = "UPDATE_GROUP";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var metadata = group.metadata();
        var metadataJson = new JsonObject(metadata);
        var updateGroupSql = queryCache.computeIfAbsent(UPDATE_GROUP_QUERY,
                k -> renderUpdateGroupSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Update Group) SQL: {}", updateGroupSql);
            log.debug("Operation(Update Group) Params: namespace({}), name({}), metadata({})",
                    namespace, name, metadata);
        }
        return pool.preparedQuery(updateGroupSql)
                .execute(Tuple.of(namespace, name)
                        .addJsonObject(metadataJson))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> true)
                .onFailure()
                .transform(e -> new FatalGroupException("Updating group unexpectedly failed.", e));
    }

    private String renderUpdateGroupSql() {
        return using(configuration)
                .update(GROUPS)
                .set(GROUPS.METADATA, field("$3", JSON.class))
                .where(GROUPS.NAMESPACE_ID.eq(namespaceSelect(configuration)))
                .and(lower(GROUPS.NAME).eq(field("$2", String.class)))
                .getSQL();
    }

    @Override
    public Uni<Boolean> delete(Group group) throws GroupException {
        return pool.withTransaction(client -> deletePermissions(client, group)
                .chain(id -> deleteGroup(client, group)));
    }

    private Uni<Boolean> deletePermissions(SqlConnection client, Group group) {
        final var DELETE_PERMISSIONS_QUERY = "DELETE_PERMISSIONS";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var deletePermissionsSql = queryCache.computeIfAbsent(DELETE_PERMISSIONS_QUERY,
                k -> renderDeletePermissionsSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Delete Group) Delete Permissions SQL: {}", deletePermissionsSql);
            log.debug("Operation(Delete Group) Delete Permissions Params: namespace({}), name({})",
                    namespace, name);
        }
        return client.preparedQuery(deletePermissionsSql)
                .execute(Tuple.of(namespace, name))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> true)
                .onFailure()
                .transform(e -> new FatalGroupException("Deleting permissions for group unexpectedly failed.", e));
    }

    private String renderDeletePermissionsSql() {
        return using(configuration)
                .deleteFrom(USER_PERMISSIONS)
                .where(USER_PERMISSIONS.GROUP_ID.eq(groupSelect(configuration)))
                .getSQL();
    }

    private Uni<Boolean> deleteGroup(SqlConnection client, Group group) {
        final var DELETE_GROUP_QUERY = "DELETE_GROUP";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var deleteGroupSql = queryCache.computeIfAbsent(DELETE_GROUP_QUERY,
                k -> renderDeleteGroupSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Delete Group) Delete Group SQL: {}", deleteGroupSql);
            log.debug("Operation(Delete Group) Delete Group Params: namespace({}), name({})",
                    namespace, name);
        }
        return client.preparedQuery(deleteGroupSql)
                .execute(Tuple.of(namespace, name))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> true)
                .onFailure()
                .transform(e -> new FatalGroupException("Deleting group unexpectedly failed.", e));
    }

    private String renderDeleteGroupSql() {
        return using(configuration)
                .deleteFrom(GROUPS)
                .where(GROUPS.NAMESPACE_ID.eq(namespaceSelect(configuration)))
                .and(lower(GROUPS.NAME).eq(field("$2", String.class)))
                .getSQL();
    }
}
