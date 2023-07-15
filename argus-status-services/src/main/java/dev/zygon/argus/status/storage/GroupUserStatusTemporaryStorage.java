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
package dev.zygon.argus.status.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.zygon.argus.group.Group;
import dev.zygon.argus.status.GroupUserStatuses;
import dev.zygon.argus.status.UserStatus;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link GroupUserStatusStorage} which stores the status
 * data within the instance. Status data is updated whenever new data is received
 * and will be evicted relatively quickly. Data should not be held for any other
 * purpose than to synchronize with a remote instance.
 *
 * @see GroupUserStatusStorage
 */
@Slf4j
@ApplicationScoped
public class GroupUserStatusTemporaryStorage implements GroupUserStatusStorage {

    @Setter(AccessLevel.PACKAGE)
    @ConfigProperty(name = "group.statuses.temporary.storage.expiration.seconds", defaultValue = "1")
    private int localStorageExpirationSeconds;

    private final Map<Group, Cache<UUID, UserStatus>> statusesByGroup;

    public GroupUserStatusTemporaryStorage() {
        this.statusesByGroup = new ConcurrentHashMap<>();
    }

    @Override
    public void track(Group group, UserStatus status) {
        var groupStatuses = findStatuses(group);
        groupStatuses.put(status.source(), status);
    }

    private Cache<UUID, UserStatus> findStatuses(Group group) {
        statusesByGroup.putIfAbsent(group, CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(localStorageExpirationSeconds))
                .build());
        return statusesByGroup.get(group);
    }

    @Override
    public Set<GroupUserStatuses> collect() {
        var collected = new HashSet<GroupUserStatuses>();
        for (var groupStatuses : statusesByGroup.entrySet()) {
            var group = groupStatuses.getKey();
            var statuses = groupStatuses.getValue();
            var statusesMap = statuses.asMap();
            var userStatuses = new HashSet<>(statusesMap.values());
            collected.add(new GroupUserStatuses(group, userStatuses));
        }
        return collected;
    }
}
