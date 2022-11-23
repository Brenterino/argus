package dev.zygon.argus.group;

import lombok.Builder;
import lombok.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Record which represents an Argus group. Users are organized into groups
 * which they have permissions to interact with those groups.
 *
 * @param name the name of the group, must be unique within an Argus instance.
 * @param metadata any additional metadata that is associated with this group.
 *                 This could include permission levels required for certain
 *                 actions or other info which is relevant to group members.
 */
@Builder
public record Group(@NonNull String name, @NonNull Map<String, Object> metadata) {

    /**
     * Constructor for Group which accepts only the name of the group. This
     * implies that there is no metadata associated with this group.
     *
     * @param name the name of the group.
     */
    public Group(@NonNull String name) {
        this(name, Collections.emptyMap());
    }

    /**
     * Constructor for Group which accepts both the name of the group and
     * metadata associated with the group. Once assigned, metadata cannot
     * be added or removed.
     *
     * @param name the name of the group.
     * @param metadata the metadata assigned to the group.
     */
    public Group(@NonNull String name, @NonNull Map<String, Object> metadata) {
        this.name = name;
        this.metadata = Collections.unmodifiableMap(metadata);
    }

    /**
     * Custom implementation of the equals function. It is recommended to
     * review the documentation of {@link Object#equals(Object)} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, equality can be determined by the
     * name of the group alone.
     * </p>
     *
     * @param o   the reference object with which to compare against this
     *            object.
     * @return if this object equals the provided object per the standard
     *         contract for {@link Object#equals(Object)}.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Group group &&
                Objects.equals(name, group.name);
    }

    /**
     * Custom implementation of the hashCode function. It is recommended to
     * review the documentation of {@link Object#hashCode()} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, hashCode can be computed by the
     * name of the group alone.
     * </p>
     *
     * @return the hashCode of this record based on the standard contract for
     *          {@link Object#hashCode()}.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    /**
     * Returns a string representation of this group. It is recommended to
     * review the documentation of {@link Record#toString()} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, toString can be computed by the
     * name of the group alone.
     * </p>
     *
     * @return a string representation of the group.
     */
    @Override
    public String toString() {
        return name;
    }
}
