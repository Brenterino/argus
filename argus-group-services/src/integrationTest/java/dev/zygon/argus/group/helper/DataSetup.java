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

import static dev.zygon.argus.group.repository.impl.CommonJooqRenderer.namespaceSelect;
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

    public void createNamespaceMapping(String namespace, String mapping) {
        pool.preparedQuery(renderCreateMappingSql())
                .execute(Tuple.of(namespace, mapping))
                .await().indefinitely();
    }

    private String renderCreateMappingSql() {
        return using(configuration)
                .insertInto(NAMESPACE_MAPPINGS,
                        NAMESPACE_MAPPINGS.NAMESPACE_ID, NAMESPACE_MAPPINGS.MAPPING)
                .values(field(namespaceSelect(configuration)), field("$2", String.class))
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

    public void deleteAllNamespaceMappings() {
        delete(this::renderDeleteNamespaceMappingsSql);
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

    private String renderDeleteNamespaceMappingsSql() {
        return using(configuration)
                .deleteFrom(NAMESPACE_MAPPINGS)
                .getSQL();
    }
}
