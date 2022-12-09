package dev.zygon.argus.location.storage;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.GroupLocations;
import dev.zygon.argus.location.Locations;

import java.util.Set;

public interface GroupLocationsStorage {

    void track(Group group, Locations locations);

    Set<GroupLocations> collect();
}
