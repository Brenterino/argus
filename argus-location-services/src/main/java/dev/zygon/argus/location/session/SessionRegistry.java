package dev.zygon.argus.location.session;

import dev.zygon.argus.location.Locations;

import javax.websocket.Session;

/**
 * Represents a registry of sessions which can have {@link Session}s be added
 * or removed as necessary. Locations may also be broadcast to all sessions
 * within the registry under a certain grouping. Sessions within the registry
 * are related by a confounding factor, but this is implementation dependent.
 *
 * @param <K> the key by which sessions are grouped.
 */
public interface SessionRegistry<K> {

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
     * Broadcast location data to the sessions that are grouped by the provided
     * key.
     *
     * @param key       the grouping key for sessions to broadcast to.
     * @param locations the location data to broadcast.
     */
    void broadcast(K key, Locations locations);
}
