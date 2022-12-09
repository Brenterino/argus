package dev.zygon.argus.location.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.GroupLocations;
import dev.zygon.argus.location.Locations;
import io.smallrye.mutiny.Multi;

public interface GroupLocationsLocalRelay {

    void receive(Group group, Locations locations);

    Multi<GroupLocations> relay();

    void relay(GroupLocations locations);
}
