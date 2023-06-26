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
