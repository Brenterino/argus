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
