package dev.zygon.argus.location.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.Locations;
import jakarta.websocket.Session;

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
     * Relay all available locations for a group to that group.
     *
     * @param group     the group which the location data is to be relayed to.
     * @param locations the group locations which will be relayed.
     */
    void relay(Group group, Locations locations);

    /**
     * Replays location data that is stored for a group to a newly joining
     * session.
     *
     * @param group   the group for which data must be replayed.
     * @param session the session for which data will be replayed to.
     */
    void replay(Group group, Session session);
}
