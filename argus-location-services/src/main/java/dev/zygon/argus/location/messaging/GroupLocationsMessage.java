package dev.zygon.argus.location.messaging;

import dev.zygon.argus.location.GroupLocations;

import java.util.Set;

public record GroupLocationsMessage(Set<GroupLocations> data) {
}
