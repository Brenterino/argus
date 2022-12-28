package dev.zygon.argus.user;

import lombok.NonNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Record which represents an Argus user. Users have a unique ID assigned to
 * them which are used for identification purposes across renames.
 *
 * @param uuid     the unique ID associated with the user - the origin of which
 *                 may come from outside the Argus platform.
 * @param name     the name of the user which is used for the purposes of display
 *                 to other users.
 */
public record User(@NonNull UUID uuid, @NonNull String name) {

    /**
     * Custom implementation of the equals function. It is recommended to
     * review the documentation of {@link Object#equals(Object)} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, equality can be determined by the
     * equality of the UUID alone.
     * </p>
     *
     * @param o the reference object with which to compare against this
     *          object.
     * @return if this object equals the provided object per the standard
     * contract for {@link Object#equals(Object)}.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof User user &&
                Objects.equals(uuid, user.uuid);
    }

    /**
     * Custom implementation of the hashCode function. It is recommended to
     * review the documentation of {@link Object#hashCode()} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, hashCode can be computed by the
     * UUID alone.
     * </p>
     *
     * @return the hashCode of this record based on the standard contract for
     * {@link Object#hashCode()}.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
