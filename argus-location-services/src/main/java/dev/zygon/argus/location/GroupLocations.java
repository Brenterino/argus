package dev.zygon.argus.location;

import dev.zygon.argus.group.Group;
import lombok.NonNull;

import java.util.Objects;

/**
 * Wrapper record which contains a mapping of a group to locations for which
 * the group has access to the data. This is primarily needed in order to
 * distinguish the appropriate group for data that is received from a remote
 * source.
 *
 * @param group     the group which the location data is available to.
 * @param locations the location data which is available to the group.
 */
public record GroupLocations(@NonNull Group group, @NonNull Locations locations) {

    /**
     * Custom implementation of the equals function. It is recommended to
     * review the documentation of {@link Object#equals(Object)} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, equality can be determined by the
     * group alone.
     * </p>
     *
     * @param o the reference object with which to compare against this
     *          object.
     * @return if this object equals the provided object per the standard
     * contract for {@link Object#equals(Object)}.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof GroupLocations groupLocations &&
                Objects.equals(group, groupLocations.group);
    }

    /**
     * Custom implementation of the hashCode function. It is recommended to
     * review the documentation of {@link Object#hashCode()} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, hashCode can be computed by the
     * group alone.
     * </p>
     *
     * @return the hashCode of this record based on the standard contract for
     * {@link Object#hashCode()}.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(group);
    }
}
