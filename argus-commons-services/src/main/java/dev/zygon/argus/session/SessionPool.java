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
 * Represents a pool of sessions which can have {@link Session}s be added or
 * removed as necessary. Target data may also be broadcast to all sessions
 * within the pool. Sessions within the pool may be related by a confounding
 * factor, but this is implementation dependent.
 */
public interface SessionPool<K> {

    /**
     * Adds a session to this pool. May not be added if the session is already
     * present in the pool. In this situation, there should be no effect - but,
     * this may vary on implementation.
     *
     * @param session the session to be added to the pool.
     */
    void add(Session session);

    /**
     * Removes a session from this pool. May not be removed if the session is
     * not present in the pool. In this situation, there should be no effect -
     * but, this may vary on implementation.
     *
     * @param session the session to be removed from the pool.
     */
    void remove(Session session);

    /**
     * Broadcast the data to all the sessions in the session pool.
     *
     * @param data the data which will be distributed to all sessions
     *             in the pool.
     */
    void broadcast(K data);

    /**
     * Determine if there are any sessions in the pool which are active.
     *
     * @return if there are any sessions in the pool.
     */
    boolean active();
}
