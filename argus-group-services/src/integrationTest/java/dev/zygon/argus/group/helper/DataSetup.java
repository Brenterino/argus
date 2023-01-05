package dev.zygon.argus.group.helper;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.audit.Audit;
import dev.zygon.argus.group.repository.AuditRepository;
import dev.zygon.argus.group.repository.GroupRepository;
import dev.zygon.argus.group.repository.PermissionRepository;
import dev.zygon.argus.permission.Permission;
import dev.zygon.argus.user.NamespaceUser;
import dev.zygon.argus.user.User;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;
import org.jooq.Configuration;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;
import java.util.function.Supplier;

import static org.jooq.generated.Tables.*;
import static org.jooq.impl.DSL.*;

@ApplicationScoped
public class DataSetup {

    private final Pool pool;
    private final AuditRepository audits;
    private final GroupRepository groups;
    private final PermissionRepository permissions;
    private final Configuration configuration;

    public DataSetup(Pool pool,
                     AuditRepository audits,
                     GroupRepository groups,
                     PermissionRepository permissions,
                     Configuration configuration) {
        this.pool = pool;
        this.audits = audits;
        this.groups = groups;
        this.permissions = permissions;
        this.configuration = configuration;
    }

    public void createNamespace(String namespace) {
        pool.preparedQuery(renderCreateNamespaceSql())
                .execute(Tuple.of(namespace))
                .await().indefinitely();
    }

    private String renderCreateNamespaceSql() {
        return using(configuration)
                .insertInto(NAMESPACES,
                        NAMESPACES.ID, NAMESPACES.NAME)
                .values(defaultValue(Long.class), field("$1", String.class))
                .onDuplicateKeyIgnore()
                .getSQL();
    }

    public void createGroup(String namespace, String groupName, UUID owner) {
        var group = new Group(namespace, groupName);
        var user = new User(owner, "");
        var namespaceUser = new NamespaceUser(namespace, user);

        groups.create(group, namespaceUser)
                .await().indefinitely();
    }

    public void grantRole(String namespace, String groupName, UUID target, Permission permission) {
        var group = new Group(namespace, groupName);
        var user = new User(target, "");
        var namespaceUser = new NamespaceUser(namespace, user);

        permissions.grant(group, namespaceUser, permission)
                .await().indefinitely();
    }

    public void createAudit(String namespace, String groupName, Audit audit) {
        var group = new Group(namespace, groupName);

        audits.create(group, audit)
                .await().indefinitely();
    }

    private void delete(Supplier<String> sqlSupplier) {
        pool.query(sqlSupplier.get())
                .execute()
                .await().indefinitely();
    }

    public void deleteAllPermissions() {
        delete(this::renderDeletePermissionsSql);
    }

    public void deleteAllAudit() {
        delete(this::renderDeleteAuditSql);
    }

    public void deleteAllGroups() {
        delete(this::renderDeleteGroupsSql);
    }

    public void deleteAllNamespaces() {
        delete(this::renderDeleteNamespacesSql);
    }

    private String renderDeletePermissionsSql() {
        return using(configuration)
                .deleteFrom(USER_PERMISSIONS)
                .getSQL();
    }

    private String renderDeleteAuditSql() {
        return using(configuration)
                .deleteFrom(GROUP_AUDIT)
                .getSQL();
    }

    private String renderDeleteGroupsSql() {
        return using(configuration)
                .deleteFrom(GROUPS)
                .getSQL();
    }

    private String renderDeleteNamespacesSql() {
        return using(configuration)
                .deleteFrom(NAMESPACES)
                .getSQL();
    }
}
