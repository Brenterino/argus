package dev.zygon.argus.location;

import dev.zygon.argus.user.User;
import lombok.NonNull;

import java.util.Objects;

/**
 * Wrapper record which contains a pair of user and location data together.
 * <p>
 * The implementation of hashCode and equals only take the specified user into
 * account.
 * </p>
 *
 * @param user the user which is located at a specific location.
 * @param location the location record which contains position data for the
 *                 user.
 */
public record UserLocation(@NonNull User user, @NonNull Location location) {

    /**
     * Custom implementation of the equals function. It is recommended to
     * review the documentation of {@link Object#equals(Object)} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, equality can be determined by the
     * equality of the user alone.
     * </p>
     *
     * @param o the reference object with which to compare against this
     *          object.
     * @return if this object equals the provided object per the standard
     * contract for {@link Object#equals(Object)}.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof UserLocation userLocation &&
                Objects.equals(user, userLocation.user);
    }

    /**
     * Custom implementation of the hashCode function. It is recommended to
     * review the documentation of {@link Object#hashCode()} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, hashCode can be computed by the
     * user alone.
     * </p>
     *
     * @return the hashCode of this record based on the standard contract for
     * {@link Object#hashCode()}.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(user);
    }
}
