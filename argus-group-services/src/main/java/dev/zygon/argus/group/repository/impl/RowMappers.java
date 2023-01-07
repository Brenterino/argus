package dev.zygon.argus.group.repository.impl;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.audit.Audit;
import dev.zygon.argus.group.audit.AuditAction;
import dev.zygon.argus.group.audit.AuditLog;
import dev.zygon.argus.namespace.Namespace;
import dev.zygon.argus.permission.Permission;
import dev.zygon.argus.permission.UserPermission;
import dev.zygon.argus.permission.UserPermissions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static dev.zygon.argus.group.repository.impl.ColumnNames.*;
import static org.jooq.generated.Tables.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RowMappers {

    public static UUID uuid(Row row) {
        return row.getUUID(USER_UUID_NAME);
    }

    public static Group groupNoMetadata(Row row) {
        return group(row, Collections.emptyMap());
    }

    public static Group group(Row row) {
        var metadata = Optional
                .ofNullable(row.getJsonObject(GROUPS.METADATA.getName()))
                .map(JsonObject::getMap)
                .orElse(Collections.emptyMap());
        return group(row, metadata);
    }

    private static Group group(Row row, Map<String, Object> metadata) {
        return new Group(row.getString(NAMESPACE_NAME),
                row.getString(GROUP_NAME),
                metadata);
    }

    public static UserPermissions userPermissions(RowSet<Row> rows, int pageSize) {
        return paginated(rows, pageSize, RowMappers::userPermission, UserPermissions::new);
    }

    public static UserPermission userPermission(Row row) {
        return new UserPermission(uuid(row), permission(row));
    }

    public static Permission permission(Row row) {
        var permissions = Permission.values();
        return permissions[row.getInteger(PERMISSION_NAME)];
    }

    public static AuditLog auditLog(RowSet<Row> rows, int pageSize) {
        return paginated(rows, pageSize, RowMappers::audit, AuditLog::new);
    }

    private static <R, I> R paginated(RowSet<Row> rows, int pageSize,
                                 Function<Row, I> mapper,
                                 BiFunction<List<I>, Integer, R> constructor) {
        var pages = 1;
        var paged = new ArrayList<I>(rows.rowCount() - 1);
        for (var row : rows) {
            var count = row.getInteger(COUNT_NAME);
            if (count >= 0) {
                pages = ((count - 1) / pageSize) + 1;
            } else {
                paged.add(mapper.apply(row));
            }
        }
        return constructor.apply(paged, pages);
    }

    public static Audit audit(Row row) {
        return Audit.builder()
                .changer(row.getUUID(GROUP_AUDIT.CHANGER.getName()))
                .target(row.getUUID(GROUP_AUDIT.TARGET.getName()))
                .action(action(row))
                .permission(permission(row))
                .occurred(row.getOffsetDateTime(GROUP_AUDIT.OCCURRED.getName()))
                .build();
    }

    public static AuditAction action(Row row) {
        var actions = AuditAction.values();
        return actions[row.getInteger(GROUP_AUDIT.ACT.getName())];
    }

    public static Namespace namespace(Row row) {
        return new Namespace(row.getString(NAMESPACES.NAME.getName()));
    }
}
