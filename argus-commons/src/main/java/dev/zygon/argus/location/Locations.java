package dev.zygon.argus.location;

import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

/**
 * Wrapper record which contains a set of user location data that can be
 * received from or sent to a user.
 * <p>
 * The contents are contained within a set to avoid duplication of user data.
 * This is possible because the implementation of hashCode and equals only take
 * the specified user into account.
 * </p>
 *
 * @param data set of user location data contained in this record.
 */
public record Locations(@NonNull Set<UserLocation> data) {

    /**
     * Constructor for Locations which the data of user locations.
     *
     * @param data the user location data.
     */
    public Locations(Set<UserLocation> data) {
        this.data = data != null ? Collections.unmodifiableSet(data) :
                Collections.emptySet();
    }
}
