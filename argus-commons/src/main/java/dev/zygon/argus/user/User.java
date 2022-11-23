package dev.zygon.argus.user;

import lombok.Builder;
import lombok.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Record which represents an Argus user. Users have a unique ID assigned to
 * them which are used for identification purposes across renames. Additional
 * metadata may be tracked which can be useful for expanded purposes.
 *
 * @param uuid     the unique ID associated with the user - the origin of which
 *                 may come from outside the Argus platform.
 * @param name     the name of the user which is used for the purposes of display
 *                 to other users.
 * @param metadata any metadata associated with this user which may be useful.
 */
@Builder
public record User(@NonNull String uuid, @NonNull String name, @NonNull Map<String, Object> metadata) {

    /**
     * Constructor for User which accepts only the UUID and name of the user.
     * This implies that there is no metadata associated with this user.
     *
     * @param uuid the unique identifier for the user.
     * @param name the name of the user.
     */
    public User(@NonNull String uuid, @NonNull String name) {
        this(uuid, name, Collections.emptyMap());
    }

    /**
     * Constructor for User which accepts the UUID, name and metadata associated
     * with the user. Once assigned, metadata cannot be added or removed.
     *
     * @param uuid     the unique identifier for the user.
     * @param name     the name of the user.
     * @param metadata the metadata assigned to the user.
     */
    public User(@NonNull String uuid, @NonNull String name, Map<String, Object> metadata) {
        this.uuid = uuid;
        this.name = name;
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) :
                Collections.emptyMap();
    }

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
