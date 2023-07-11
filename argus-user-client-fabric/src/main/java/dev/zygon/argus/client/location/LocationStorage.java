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
package dev.zygon.argus.client.location;

import dev.zygon.argus.client.ArgusClient;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.name.NameStorage;
import dev.zygon.argus.location.*;
import dev.zygon.argus.user.User;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public enum LocationStorage {

    INSTANCE;

    // TODO clean up local storage and storage entries after X amount of time
    private final Map<LocationKey, Location> localStorage;
    @Getter private final Map<LocationKey, Location> storage;

    LocationStorage() {
        localStorage = new ConcurrentHashMap<>();
        storage = new ConcurrentHashMap<>();
    }

    public void trackPing(UUID source, Coordinate coordinate) {
        trackInternal(source, LocationType.BASIC_PING, coordinate);
    }

    public void trackTarget(UUID target, Coordinate coordinate) {
        trackInternal(target, LocationType.FOCUS_PING, coordinate);
    }

    public void trackPlayer(UUID source, Coordinate coordinate) {
        trackInternal(source, LocationType.USER, coordinate);
    }

    public void trackPlayerMisc(UUID source, Coordinate coordinate) {
        trackInternal(source, LocationType.MISC_USER, coordinate);
    }

    private void trackInternal(UUID target, LocationType type, Coordinate coordinate) {
        var user = new User(target, resolveNameFromUUIDAndType(target, type));
        var location = new Location(user, type, coordinate);
        var key = location.key();
        localStorage.put(key, location);
        storage.put(key, location);
    }

    private String resolveNameFromUUIDAndType(UUID target, LocationType type) {
        var name = NameStorage.INSTANCE.nameFromId(target);
        return switch (type) {
            case USER, MISC_USER -> name;
            case BASIC_PING -> name + "'s Ping";
            case FOCUS_PING -> "Focus " + name;
        };
    }

    public void fromRemote(Locations locations) {
        for (var location : locations.data()) {
            var key = location.key();
            if (storage.containsKey(key)) {
                var current = storage.get(key);
                var oldTime = current.coordinates().time();
                var newTime = location.coordinates().time();
                if (oldTime.isBefore(newTime)) {
                    // TODO possibly prioritize local over timestamp given a certain tolerance
                    // primarily for people who are sending their data but there might be slightly
                    // conflicting data due to other sources
                    storage.put(key, location);
                }
            } else {
                storage.put(key, location);
            }
        }
    }

    public void syncRemote(ArgusClient client) {
        var locations = new HashSet<Location>();
        localStorage.keySet().forEach(k -> localStorage.compute(k, (ki, vi) -> {
            locations.add(vi);
            return null;
        }));
        if (!locations.isEmpty()) {
            client.getLocations().sendLocations(new Locations(locations));
        }
    }

    public void cleanLocations() {
        var config = ArgusClientConfig.getActiveConfig();
        var now = Instant.now();
        storage.keySet().forEach(k -> storage.compute(k, (ki, vi) -> {
            if (vi != null) {
                var coordinates = vi.coordinates();
                var duration = Duration.between(coordinates.time(), now);
                if (duration.toMinutes() >= config.getLocationsExpirationMinutes()) {
                    return null;
                }
            }
            return vi;
        }));
    }

    public void clean() {
        localStorage.clear();
        storage.clear();
    }
}
