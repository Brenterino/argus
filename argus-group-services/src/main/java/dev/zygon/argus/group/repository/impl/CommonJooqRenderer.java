package dev.zygon.argus.group.repository.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jooq.Configuration;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;

import static org.jooq.generated.Tables.GROUPS;
import static org.jooq.generated.Tables.NAMESPACES;
import static org.jooq.impl.DSL.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonJooqRenderer {

    public static SelectConditionStep<Record1<Long>> groupSelect(Configuration configuration) {
        return using(configuration)
                .select(GROUPS.ID)
                .from(GROUPS)
                .where(GROUPS.NAMESPACE_ID.eq(namespaceSelect(configuration)))
                .and(lower(GROUPS.NAME).eq(field("$2", String.class)));
    }

    public static SelectConditionStep<Record1<Long>> namespaceSelect(Configuration configuration) {
        return using(configuration)
                .select(NAMESPACES.ID)
                .from(NAMESPACES)
                .where(NAMESPACES.NAME.eq(field("$1", String.class)));
    }
}
