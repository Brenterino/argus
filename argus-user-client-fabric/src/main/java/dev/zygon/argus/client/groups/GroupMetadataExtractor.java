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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.util.UUIDTypeAdapter;
import dev.zygon.argus.group.Group;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public class GroupMetadataExtractor {

    private GroupMetadataExtractor() {
    }

    private static final Gson MAPPER = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
            .create();

    public static GroupMetadata fromGroup(Group group) {
        try {
            var element = MAPPER.toJsonTree(group.metadata());
            return MAPPER.fromJson(element, GroupMetadata.class);
        } catch (Throwable e) { // because GSON can throw Errors as well as Exceptions! :)
            log.warn("[ARGUS] Failed parsing metadata for group {}!", group, e);
            return new GroupMetadata(List.of(), List.of());
        }
    }
}
