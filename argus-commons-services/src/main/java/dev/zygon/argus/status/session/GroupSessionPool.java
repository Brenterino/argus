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
package dev.zygon.argus.status.session;

import dev.zygon.argus.group.Group;
import jakarta.websocket.SendResult;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link SessionPool} which pools sessions in groups.
 *
 * @see SessionPool
 */
@Slf4j
public class GroupSessionPool<K> implements SessionPool<K> {

    private final Group group;
    private final Map<String, Session> sessions;

    public GroupSessionPool(Group group) {
        this.group = group;
        this.sessions = new ConcurrentHashMap<>();
    }

    @Override
    public void add(Session session) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to add session with ID ({}) to group ({})",
                    session.getId(), group);
        }
        if (sessions.putIfAbsent(session.getId(), session) != null) {
            log.warn("Session with duplicate ID ({}) was attempted to be added to group ({})",
                    session.getId(), group);
        }
    }

    @Override
    public void remove(Session session) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to remove session with ID ({}) to group ({})",
                    session.getId(), group);
        }
        if (sessions.remove(session.getId()) == null) {
            log.warn("Attempted to remove non-existing session with ID ({}) from group ({})",
                    session.getId(), group);
        }
    }

    @Override
    public void broadcast(K data) {
        if (log.isDebugEnabled()) {
            log.debug("Broadcasting data to group ({}): {}",
                    group, data);
        }
        sessions.values()
                .stream()
                .map(Session::getAsyncRemote)
                .forEach(async -> async.sendObject(data, this::broadcastCallback));
    }

    private void broadcastCallback(SendResult result) {
        if (result.getException() != null) {
            log.error("Unable to broadcast on group {}.",
                    group, result.getException());
        }
    }

    @Override
    public boolean active() {
        return !sessions.isEmpty();
    }
}
