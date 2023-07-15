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
package dev.zygon.argus.status.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.status.UserStatus;
import dev.zygon.argus.status.session.SessionRegistry;
import dev.zygon.argus.status.storage.GroupUserStatusStorage;
import io.quarkus.arc.profile.IfBuildProfile;
import io.smallrye.mutiny.Multi;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.time.Duration;

/**
 * Implementation of {@link GroupStatusesRemoteSynchronizer} which uses the
 * local storage for the purpose of synchronizing status data across multiple
 * instances. This synchronizer is only active when the build profile is set to
 * 'scale' as this is the profile which indicates multiple instances will be
 * spun up. The implementation uses MP reactive messaging which does not have
 * a local pair. It is important that the corresponding pair of the incoming or
 * outgoing portion is configured properly.
 */
@IfBuildProfile("scale")
@ApplicationScoped
public class GroupStatusesRemoteRelaySynchronizer implements GroupStatusesRemoteSynchronizer {

    @Setter(AccessLevel.PACKAGE)
    @ConfigProperty(name = "group.statuses.relay.remote.publish.delay.millis", defaultValue = "100")
    private long remoteRelayPublishDelayMillis;

    private final GroupUserStatusStorage storage;
    private final SessionRegistry<Group, UserStatus> registry;

    public GroupStatusesRemoteRelaySynchronizer(GroupUserStatusStorage storage,
                                                SessionRegistry<Group, UserStatus> registry) {
        this.storage = storage;
        this.registry = registry;
    }

    @Override
    @Incoming("statuses-in")
    public void receive(JsonObject rawMessage) {
        var message = rawMessage.mapTo(UserStatusesMessage.class);
        for (var groupUserStatuses : message.data()) {
            var group = groupUserStatuses.group();
            var statuses = groupUserStatuses.statuses();

            statuses.forEach(status -> registry.broadcast(group, status));
        }
    }

    @Override
    @Outgoing("statuses-out")
    public Multi<UserStatusesMessage> send() {
        return Multi.createFrom()
                .ticks()
                .every(Duration.ofMillis(remoteRelayPublishDelayMillis))
                .map(t -> storage.collect())
                .map(UserStatusesMessage::new)
                .onOverflow()
                .drop();
    }
}
