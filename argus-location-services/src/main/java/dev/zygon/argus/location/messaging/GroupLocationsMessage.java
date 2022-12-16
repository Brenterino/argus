package dev.zygon.argus.location.messaging;

import dev.zygon.argus.location.GroupLocations;

import java.util.Set;

/**
 * Wrapper record which contains a set of {@link GroupLocations} records which
 * is sent between instances via the {@link GroupLocationsRemoteSynchronizer}.
 * This record is only meant to be used as a wrapper because it is not possible
 * to directly serialize a {@link Set} into JSON.
 *
 * @param data set of all group locations. Uniqueness is guaranteed by the
 *             implementation of {@link GroupLocations#hashCode()}
 */
public record GroupLocationsMessage(Set<GroupLocations> data) {
}
