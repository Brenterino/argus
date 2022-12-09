package dev.zygon.argus.location.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.storage.GroupLocationsStorage;
import io.smallrye.mutiny.Multi;
import lombok.AccessLevel;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;

@ApplicationScoped
public class GroupLocationsRemoteRelay {

    @Setter(AccessLevel.PACKAGE)
    @ConfigProperty(name = "group.locations.relay.remote.publish.delay.millis", defaultValue = "1000")
    private long remoteRelayPublishDelayMillis;

    private final GroupLocationsStorage storage;

    public GroupLocationsRemoteRelay(GroupLocationsStorage storage) {
        this.storage = storage;
    }

    public void send(Group group, Locations locations) {
        storage.track(group, locations);
    }

    @Outgoing("locations-out")
    public Multi<GroupLocationsMessage> publish() {
        return Multi.createFrom()
                .ticks()
                .every(Duration.ofMillis(remoteRelayPublishDelayMillis))
                .map(t -> storage.collect())
                .map(GroupLocationsMessage::new)
                .onOverflow()
                .drop();
    }
}
