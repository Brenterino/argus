package dev.zygon.argus.group.repository.impl;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.permission.Permission;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static dev.zygon.argus.group.repository.impl.ColumnNames.*;
import static org.jooq.generated.Tables.GROUPS;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RowMappers {

    public static UUID uuid(Row row) {
        return row.getUUID(USER_UUID_NAME);
    }

    public static Group group(Row row) {
        var column = row.getColumnIndex(GROUPS.METADATA.getName());
        var metadata = column > 0 ? Optional
                .ofNullable(row.getJsonObject(GROUPS.METADATA.getName()))
                .map(JsonObject::getMap)
                .orElse(Collections.emptyMap()) :
                Collections.<String, Object>emptyMap();
        return new Group(row.getString(NAMESPACE_NAME),
                row.getString(GROUP_NAME),
                metadata);
    }

    public static Permission permission(Row row) {
        var permissions = Permission.values();
        return permissions[row.getInteger(PERMISSION_NAME)];
    }
}
