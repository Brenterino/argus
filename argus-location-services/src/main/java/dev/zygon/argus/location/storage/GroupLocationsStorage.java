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
package dev.zygon.argus.location.storage;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.GroupLocations;
import dev.zygon.argus.location.Locations;

import java.util.Set;

/**
 * Abstraction for storage which can be used to store location data for groups.
 */
public interface GroupLocationsStorage {

    /**
     * Track location data for a certain group.
     *
     * @param group     the group to track the location data for.
     * @param locations the location data to be tracked.
     */
    void track(Group group, Locations locations);

    /**
     * Retrieve the locations for a certain group.
     *
     * @param group the group to retrieve location data for.
     *
     * @return the locations that are available for this
     *         group.
     */
    Locations locations(Group group);

    /**
     * Retrieve the location data in the form of a set of {@link GroupLocations}
     * records which relate a group to a set of locations.
     *
     * @return a representation of the current state of group locations in
     * storage.
     */
    Set<GroupLocations> collect();
}
