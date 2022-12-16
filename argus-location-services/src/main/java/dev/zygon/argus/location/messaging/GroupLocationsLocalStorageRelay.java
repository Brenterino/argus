package dev.zygon.argus.location.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.GroupLocations;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.session.SessionRegistry;
import dev.zygon.argus.location.storage.GroupLocationsStorage;
import io.smallrye.mutiny.Multi;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;

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

    @Setter(AccessLevel.PACKAGE)
    @ConfigProperty(name = "group.locations.relay.local.publish.delay.millis", defaultValue = "100")
    private long localRelayPublishDelayMillis;

    private final GroupLocationsStorage storage;
    private final SessionRegistry<Group> registry;

    public GroupLocationsLocalStorageRelay(GroupLocationsStorage storage,
                                           SessionRegistry<Group> registry) {
        this.storage = storage;
        this.registry = registry;
    }

    @Override
    public void receive(Group group, Locations locations) {
        storage.track(group, locations);
    }

    @Override
    @Outgoing("local-locations")
    public Multi<GroupLocations> relay() {
        return Multi.createFrom()
                .ticks()
                .every(Duration.ofMillis(localRelayPublishDelayMillis))
                .map(t -> storage.collect())
                .flatMap(s -> Multi.createFrom().iterable(s))
                .onOverflow()
                .drop();
    }

    @Override
    @Incoming("local-locations")
    public void relay(GroupLocations locations) {
        var group = locations.group();
        var data = locations.locations();
        registry.broadcast(group, data);
    }
}
