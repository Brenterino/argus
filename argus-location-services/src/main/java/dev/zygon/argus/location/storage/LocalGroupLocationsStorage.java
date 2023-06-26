package dev.zygon.argus.location.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.*;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of {@link GroupLocationsStorage} which stores the location
 * data within the instance. Location data is updated by utilizing
 * {@link LocationPriorityStrategy} to determine if the currently held location
 * data needs to be evicted and replace with newly received data. Data may be
 * tracked for a particular {@link LocationKey}, but will eventually be evicted.
 *
 * @see GroupLocationsStorage
 * @see LocationPriorityStrategy
 */
@Slf4j
@ApplicationScoped
public class LocalGroupLocationsStorage implements GroupLocationsStorage {

    @Setter(AccessLevel.PACKAGE)
    @ConfigProperty(name = "group.locations.local.storage.expiration.minutes", defaultValue = "10")
    private int localStorageExpirationMinutes;

    private final LocationPriorityStrategy strategy;
    private final Map<Group, Cache<LocationKey, Coordinate>> locationsByGroup;

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
        var previous = locations.getIfPresent(key);
        if (strategy.shouldReplace(previous, data)) {
            locations.put(key, data);
        }
    }

    @Override
    public Locations locations(Group group) {
        var locations = findLocations(group);
        return convertToLocations(locations);
    }

    private Cache<LocationKey, Coordinate> findLocations(Group group) {
        locationsByGroup.putIfAbsent(group, CacheBuilder.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(localStorageExpirationMinutes))
                .build());
        return locationsByGroup.get(group);
    }

    @Override
    public Set<GroupLocations> collect() {
        var collected = new HashSet<GroupLocations>();
        for (var groupLocation : locationsByGroup.entrySet()) {
            var group = groupLocation.getKey();
            var locations = groupLocation.getValue();
            var locationsData = convertToLocations(locations);
            collected.add(new GroupLocations(group, locationsData));
        }
        return collected;
    }

    private Locations convertToLocations(Cache<LocationKey, Coordinate> data) {
        var userLocations = data.asMap()
                .entrySet()
                .stream()
                .map(e -> new Location(e.getKey().user(), e.getKey().type(), e.getValue()))
                .collect(Collectors.toUnmodifiableSet());
        return new Locations(userLocations);
    }
}
