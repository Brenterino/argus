package dev.zygon.argus.location.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.session.SessionRegistry;
import dev.zygon.argus.location.storage.GroupLocationsStorage;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link GroupLocationsLocalRelay} which is backed by local
 * storage to store current location data within. The relay {@link Multi} will
 * be used to push messages which can then be distributed to the appropriate
 * group by broadcasting via {@link SessionRegistry}.
 *
 * @see GroupLocationsLocalRelay
 */
@Slf4j
@ApplicationScoped
public class GroupLocationsLocalStorageRelay implements GroupLocationsLocalRelay {

    private final GroupLocationsStorage storage;
    private final SessionRegistry<Group> registry;

    public GroupLocationsLocalStorageRelay(GroupLocationsStorage storage,
                                           SessionRegistry<Group> registry) {
        this.storage = storage;
        this.registry = registry;
    }

    @Override
    public void receive(Group group, Locations locations) {
        if (log.isDebugEnabled()) {
            log.debug("Received message for group ({}): {}",
                    group, locations);
        }
        storage.track(group, locations);
        relay(group, locations);
    }

    @Override
    public void relay(Group group, Locations locations) {
        if (log.isDebugEnabled()) {
            log.debug("Relaying message for group ({}): {}",
                    group, locations);
        }
        registry.broadcast(group, locations);
    }

    @Override
    public void replay(Group group, Session session) {
        if (log.isDebugEnabled()) {
            log.debug("Replaying messages for group ({}) to {}",
                    group, session.getId());
        }
        var locations = storage.locations(group);

        session.getAsyncRemote()
                .sendObject(locations);
    }
}
