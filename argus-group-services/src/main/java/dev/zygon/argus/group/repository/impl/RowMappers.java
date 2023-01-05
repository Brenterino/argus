package dev.zygon.argus.group.repository.impl;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.audit.Audit;
import dev.zygon.argus.group.audit.AuditAction;
import dev.zygon.argus.group.audit.AuditLog;
import dev.zygon.argus.permission.Permission;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;

import static dev.zygon.argus.group.repository.impl.ColumnNames.*;
import static org.jooq.generated.Tables.GROUPS;
import static org.jooq.generated.Tables.GROUP_AUDIT;

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

    public static Permission permission(Row row) {
        var permissions = Permission.values();
        return permissions[row.getInteger(PERMISSION_NAME)];
    }

    public static AuditLog auditLog(RowSet<Row> rows, int pageSize) {
        var pages = 1;
        var logs = new ArrayList<Audit>(rows.rowCount() - 1);
        for (var row : rows) {
            var count = row.getInteger(COUNT_NAME);
            if (count >= 0) {
                pages = ((count - 1) / pageSize) + 1;
            } else {
                logs.add(audit(row));
            }
        }
        return new AuditLog(logs, pages);
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
}
