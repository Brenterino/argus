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
package dev.zygon.argus.session;

import dev.zygon.argus.group.Group;
import jakarta.websocket.Session;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link SessionRegistry} which groups sessions by the
 * {@link Group} record type.
 *
 * @see SessionRegistry
 */
@Slf4j
public class GroupSessionRegistry<V> implements SessionRegistry<Group, V> {

    @Getter(AccessLevel.PACKAGE)
    private final Map<Group, GroupSessionPool<V>> pools;

    public GroupSessionRegistry() {
        this.pools = new ConcurrentHashMap<>();
    }

    @Override
    public void add(Group group, Session session) {
        var pool = findPool(group);
        pool.add(session);
    }

    @Override
    public void remove(Group group, Session session) {
        var pool = findPool(group);
        pool.remove(session);
    }

    @Override
    public void broadcast(Group group, V data) {
        var pool = findPool(group);
        if (pool.active()) {
            pool.broadcast(data);
        }
    }

    private SessionPool<V> findPool(Group group) {
        pools.putIfAbsent(group, new GroupSessionPool<V>(group));

        return pools.get(group);
    }
}
