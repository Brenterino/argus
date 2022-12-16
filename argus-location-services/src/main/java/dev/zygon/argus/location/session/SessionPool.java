package dev.zygon.argus.location.session;

import dev.zygon.argus.location.Locations;

import javax.websocket.Session;

/**
 * Represents a pool of sessions which can have {@link Session}s be added or
 * removed as necessary. Locations may also be broadcast to all sessions
 * within the pool. Sessions within the pool may be related by a confounding
 * factor, but this is implementation dependent.
 */
public interface SessionPool {

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
     * Broadcast the locations to all the sessions in the session pool.
     *
     * @param locations the location data which will be distributed to
     *                  all sessions in the pool.
     */
    void broadcast(Locations locations);

    /**
     * Determine if there are any sessions in the pool which are active.
     *
     * @return if there are any sessions in the pool.
     */
    boolean active();
}
