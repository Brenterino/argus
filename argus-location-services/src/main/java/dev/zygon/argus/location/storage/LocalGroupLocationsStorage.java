package dev.zygon.argus.location.storage;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;
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
    private final Map<Group, Map<LocationKey, Coordinate>> locationsByGroup;

    public LocalGroupLocationsStorage(LocationPriorityStrategy strategy) {
        this.strategy = strategy;
        this.locationsByGroup = new ConcurrentHashMap<>();
    }

    @Override
    public void track(Group group, Locations locations) {
        locations.data()
                .forEach(location -> updateLocation(group, location));
    }

    private void updateLocation(Group group, Location location) {
        var locations = findLocations(group);
        var key = location.key();
        var data = location.coordinates();
        var previous = locations.putIfAbsent(key, data);
        while (previous != null && strategy.shouldReplace(previous, data)) { // threading conditions, may choose to do this differently, though
            locations.remove(key);
            previous = locations.putIfAbsent(key, data);
        }
    }

    private Map<LocationKey, Coordinate> findLocations(Group group) {
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
                    .map(e -> new Location(e.getKey().user(), e.getKey().type(), e.getValue()))
                    .collect(Collectors.toUnmodifiableSet());
            var locationsData = new Locations(userLocations);
            collected.add(new GroupLocations(group, locationsData));
        }
        return collected;
    }
}
