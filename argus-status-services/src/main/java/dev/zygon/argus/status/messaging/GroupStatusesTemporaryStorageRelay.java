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
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link UserStatusLocalRelay} which is backed by temporary
 * storage to store status data in to relay remotely (if applicable). The relay
 * will be used to push messages which can then be distributed to the appropriate
 * group by broadcasting via {@link SessionRegistry}.
 *
 * @see UserStatusLocalRelay
 */
@Slf4j
@ApplicationScoped
public class GroupStatusesTemporaryStorageRelay implements UserStatusLocalRelay {

    private final GroupUserStatusStorage storage;
    private final SessionRegistry<Group, UserStatus> registry;

    public GroupStatusesTemporaryStorageRelay(GroupUserStatusStorage storage,
                                              SessionRegistry<Group, UserStatus> registry) {
        this.storage = storage;
        this.registry = registry;
    }

    @Override
    public void receive(Group group, UserStatus status) {
        if (log.isDebugEnabled()) {
            log.debug("Received message for group ({}): {}",
                    group, status);
        }
        storage.track(group, status);
        relay(group, status);
    }

    @Override
    public void relay(Group group, UserStatus status) {
        if (log.isDebugEnabled()) {
            log.debug("Relaying message for group ({}): {}",
                    group, status);
        }
        registry.broadcast(group, status);
    }
}
