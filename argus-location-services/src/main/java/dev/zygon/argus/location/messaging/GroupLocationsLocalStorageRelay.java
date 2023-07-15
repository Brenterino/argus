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
package dev.zygon.argus.location.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.storage.GroupLocationsStorage;
import dev.zygon.argus.status.session.SessionRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link GroupLocationsLocalRelay} which is backed by local
 * storage to store current location data within. The relay will be used to push
 * messages which can then be distributed to the appropriate group by broadcasting
 * via {@link SessionRegistry}.
 *
 * @see GroupLocationsLocalRelay
 */
@Slf4j
@ApplicationScoped
public class GroupLocationsLocalStorageRelay implements GroupLocationsLocalRelay {

    private final GroupLocationsStorage storage;
    private final SessionRegistry<Group, Locations> registry;

    public GroupLocationsLocalStorageRelay(GroupLocationsStorage storage,
                                           SessionRegistry<Group, Locations> registry) {
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
