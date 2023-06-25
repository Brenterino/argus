package dev.zygon.argus.location.session;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.Locations;
import lombok.extern.slf4j.Slf4j;

import jakarta.websocket.SendResult;
import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link SessionPool} which pools sessions in groups.
 *
 * @see SessionPool
 */
@Slf4j
public class GroupSessionPool implements SessionPool {

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
    public void broadcast(Locations locations) {
        if (log.isDebugEnabled()) {
            log.debug("Broadcasting locations to group ({}): {}",
                    group, locations);
        }
        sessions.values()
                .stream()
                .map(Session::getAsyncRemote)
                .forEach(async -> async.sendObject(locations, this::broadcastCallback));
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
