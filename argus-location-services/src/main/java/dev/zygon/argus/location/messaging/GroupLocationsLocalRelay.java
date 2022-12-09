package dev.zygon.argus.location.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.GroupLocations;
import dev.zygon.argus.location.session.SessionRegistry;
import dev.zygon.argus.location.storage.GroupLocationsStorage;
import io.smallrye.mutiny.Multi;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;

@Slf4j
@ApplicationScoped
public class GroupLocationsLocalRelay {

    @Setter(AccessLevel.PACKAGE)
    @ConfigProperty(name = "group.locations.relay.local.publish.delay.millis", defaultValue = "1000")
    private long localRelayPublishDelayMillis;

    private final GroupLocationsStorage storage;
    private final SessionRegistry<Group> registry;

    public GroupLocationsLocalRelay(GroupLocationsStorage storage,
                                    SessionRegistry<Group> registry) {
        this.storage = storage;
        this.registry = registry;
    }

    @Incoming("locations-in")
    public void receive(JsonObject rawMessage) {
        var message = rawMessage.mapTo(GroupLocationsMessage.class);
        for (var groupLocations : message.data()) {
            var group = groupLocations.group();
            var locations = groupLocations.locations();

            storage.track(group, locations);
        }
    }

    @Outgoing("local-locations")
    public Multi<GroupLocations> publishLocal() {
        return Multi.createFrom()
                .ticks()
                .every(Duration.ofMillis(localRelayPublishDelayMillis))
                .map(t -> storage.collect())
                .flatMap(s -> Multi.createFrom().iterable(s))
                .onOverflow()
                .drop();
    }

    @Incoming("local-locations")
    public void relayLocal(GroupLocations locations) {
        var group = locations.group();
        var data = locations.locations();
        registry.broadcast(group, data);
    }
}
