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
package dev.zygon.argus.client.groups;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMetadata {

    private List<GroupAlignment> alignments;
    private List<GroupCategory> categories;

    public Map<UUID, GroupAlignment> idToAlignment() {
        return Optional.ofNullable(alignments).stream().flatMap(Collection::stream)
                .collect(Collectors.toMap(GroupAlignment::getUuid, Function.identity()));
    }

    public Map<String, GroupCategory> nameToCategory() {
        return Optional.ofNullable(categories).stream().flatMap(Collection::stream)
                .collect(Collectors.toMap(GroupCategory::getName, Function.identity()));
    }

    public Map<UUID, GroupAlignmentDisplay> displays() {
        var nameToCategory = nameToCategory();
        return Optional.ofNullable(alignments).stream().flatMap(Collection::stream)
                .map(alignment -> fromAlignment(alignment, nameToCategory))
                .collect(Collectors.toMap(GroupAlignmentDisplay::target, Function.identity()));
    }

    private GroupAlignmentDisplay fromAlignment(GroupAlignment alignment, Map<String, GroupCategory> nameToCategory) {
        var category = nameToCategory.getOrDefault(alignment.getAlignment(),
                GroupCategory.DEFAULT_CATEGORY);
        return new GroupAlignmentDisplay(alignment.getUuid(), alignment.getName(),
                category.getSymbol(), category.realColor());
    }
}
