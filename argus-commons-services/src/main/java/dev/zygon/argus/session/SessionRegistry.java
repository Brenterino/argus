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

import jakarta.websocket.Session;

/**
 * Represents a registry of sessions which can have {@link Session}s be added
 * or removed as necessary. Data may also be broadcast to all sessions within
 * the registry under a certain grouping. Sessions within the registry are
 * related by a confounding factor, but this is implementation dependent.
 *
 * @param <K> the key by which sessions are grouped.
 * @param <V> the type of data which can be sent.
 */
public interface SessionRegistry<K, V> {

    /**
     * Register a session based on the key.
     *
     * @param key     the key to register the session under.
     * @param session the session to be registered.
     */
    void add(K key, Session session);

    /**
     * Unregister a session based on the key.
     *
     * @param key     the key to register the session under.
     * @param session the session to be unregistered.
     */
    void remove(K key, Session session);

    /**
     * Broadcast data to the sessions that are grouped by the provided
     * key.
     *
     * @param key  the grouping key for sessions to broadcast to.
     * @param data the data to broadcast.
     */
    void broadcast(K key, V data);
}
