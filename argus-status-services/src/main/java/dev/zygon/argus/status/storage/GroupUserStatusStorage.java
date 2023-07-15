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
package dev.zygon.argus.status.storage;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.status.GroupUserStatuses;
import dev.zygon.argus.status.UserStatus;

import java.util.Set;

/**
 * Abstraction for storage which can be used to store status data for groups.
 */
public interface GroupUserStatusStorage {

    /**
     * Track status data for a certain group.
     *
     * @param group  the group to track the status data for.
     * @param status the status data to be tracked.
     */
    void track(Group group, UserStatus status);

    /**
     * Retrieve the status data in the form of a set of {@link GroupUserStatuses}
     * records which relate a group to a set of statuses.
     *
     * @return a representation of the current state of group user statuses in
     * storage.
     */
    Set<GroupUserStatuses> collect();
}
