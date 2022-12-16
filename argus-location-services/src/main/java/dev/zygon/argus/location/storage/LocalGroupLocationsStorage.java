package dev.zygon.argus.location.storage;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.GroupLocations;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.UserLocation;
import dev.zygon.argus.user.User;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of {@link GroupLocationsStorage} which stores the location
 * data within the instance. Location data is updated by utilizing
 * {@link LocationPriorityStrategy} to determine if the currently held location
 * data needs to be evicted and replace with newly received data.
 *
 * @see GroupLocationsStorage
 * @see LocationPriorityStrategy
 */
@Slf4j
@ApplicationScoped
public class LocalGroupLocationsStorage implements GroupLocationsStorage {

    private final LocationPriorityStrategy strategy;
    private final Map<Group, Map<User, Location>> locationsByGroup;

    public LocalGroupLocationsStorage(LocationPriorityStrategy strategy) {
        this.strategy = strategy;
        this.locationsByGroup = new ConcurrentHashMap<>();
    }

    @Override
    public void track(Group group, Locations locations) {
        locations.data()
                .forEach(location -> updateLocation(group, location));
    }

    private void updateLocation(Group group, UserLocation location) {
        var locations = findLocations(group);
        var user = location.user();
        var data = location.location();
        var previous = locations.putIfAbsent(user, data);
        while (previous != null && strategy.shouldReplace(previous, data)) { // threading conditions, may choose to do this differently, though
            locations.remove(user);
            previous = locations.putIfAbsent(user, data);
        }
    }

    private Map<User, Location> findLocations(Group group) {
        locationsByGroup.putIfAbsent(group, new ConcurrentHashMap<>());

        return locationsByGroup.get(group);
    }

    @Override
    public Set<GroupLocations> collect() {
        var collected = new HashSet<GroupLocations>();
        for (var groupLocation : locationsByGroup.entrySet()) {
            var group = groupLocation.getKey();
            var locations = groupLocation.getValue();
            var userLocations = locations.entrySet()
                    .stream()
                    .map(e -> new UserLocation(e.getKey(), e.getValue()))
                    .collect(Collectors.toUnmodifiableSet());
            var locationsData = new Locations(userLocations);
            collected.add(new GroupLocations(group, locationsData));
        }
        return collected;
    }
}
