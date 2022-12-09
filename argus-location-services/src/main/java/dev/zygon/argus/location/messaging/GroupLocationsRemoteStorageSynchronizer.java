package dev.zygon.argus.location.messaging;

import dev.zygon.argus.location.storage.GroupLocationsStorage;
import io.quarkus.arc.profile.IfBuildProfile;
import io.smallrye.mutiny.Multi;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;

@IfBuildProfile("scale")
@ApplicationScoped
public class GroupLocationsRemoteStorageSynchronizer implements GroupLocationsRemoteSynchronizer {

    @Setter(AccessLevel.PACKAGE)
    @ConfigProperty(name = "group.locations.relay.remote.publish.delay.millis", defaultValue = "1000")
    private long remoteRelayPublishDelayMillis;

    private final GroupLocationsStorage storage;

    public GroupLocationsRemoteStorageSynchronizer(GroupLocationsStorage storage) {
        this.storage = storage;
    }

    @Override
    @Incoming("locations-in")
    public void receive(JsonObject rawMessage) {
        var message = rawMessage.mapTo(GroupLocationsMessage.class);
        for (var groupLocations : message.data()) {
            var group = groupLocations.group();
            var locations = groupLocations.locations();

            storage.track(group, locations);
        }
    }

    @Override
    @Outgoing("locations-out")
    public Multi<GroupLocationsMessage> send() {
        return Multi.createFrom()
                .ticks()
                .every(Duration.ofMillis(remoteRelayPublishDelayMillis))
                .map(t -> storage.collect())
                .map(GroupLocationsMessage::new)
                .onOverflow()
                .drop();
    }
}
