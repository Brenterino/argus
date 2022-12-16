package dev.zygon.argus.location.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.GroupLocations;
import dev.zygon.argus.location.Locations;
import io.smallrye.mutiny.Multi;

/**
 * Abstraction for a local relay which can send and receive messages to/from
 * groups of users.
 */
public interface GroupLocationsLocalRelay {

    /**
     * Called upon reception of a group location message. Handling may vary
     * based on implementation.
     *
     * @param group     the group which the location data is received from.
     * @param locations the location data which was received for the group.
     */
    void receive(Group group, Locations locations);

    /**
     * Create a {@link Multi} which is used to relay group locations to the
     * appropriate groups.
     *
     * @return a {@link Multi} which can be used to relay messages to the
     * appropriate groups.
     */
    Multi<GroupLocations> relay();

    /**
     * Relay all available locations for a group to that group.
     *
     * @param locations the group locations which will be relayed.
     */
    void relay(GroupLocations locations);
}
