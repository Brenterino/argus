package dev.zygon.argus.location.session;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.Locations;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link SessionRegistry} which groups sessions by the
 * {@link Group} record type.
 *
 * @see SessionRegistry
 */
@Slf4j
@ApplicationScoped
public class GroupSessionRegistry implements SessionRegistry<Group> {

    @Getter(AccessLevel.PACKAGE)
    private final Map<Group, GroupSessionPool> pools;

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
    public void broadcast(Group group, Locations locations) {
        var pool = findPool(group);
        if (pool.active()) {
            pool.broadcast(locations);
        }
    }

    private SessionPool findPool(Group group) {
        pools.putIfAbsent(group, new GroupSessionPool(group));

        return pools.get(group);
    }
}
