package dev.zygon.argus.group;

import lombok.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Record which represents an Argus group. Users are organized into groups
 * which they have permissions to interact with those groups. Groups are
 * organized into namespaces for which they must be unique.
 *
 * @param namespace the namespace of the group, must be unique within an Argus instance.
 * @param name      the name of the group, must be unique within a namespace.
 * @param metadata  any additional metadata that is associated with this group.
 *                  This could include permission levels required for certain
 *                  actions or other info which is relevant to group members.
 */
public record Group(@NonNull String namespace, @NonNull String name, @NonNull Map<String, Object> metadata) {

    private static final String DEFAULT_NAMESPACE = "DEFAULT";
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^\\w+$");

    /**
     * Constructor for Group which accepts only the name of the group. This
     * implies usage of the default namespace and that there is no metadata
     * associated with this group.
     *
     * @param name the name of the group.
     */
    public Group(@NonNull String name) {
        this(DEFAULT_NAMESPACE, name, Collections.emptyMap());
    }

    /**
     * Constructor for Group which accepts only the fully qualified name of the
     * group. This implies that there is no metadata associated with this group.
     *
     * @param namespace the namespace of the group.
     * @param name      the name of the group.
     */
    public Group(@NonNull String namespace, @NonNull String name) {
        this(namespace, name, Collections.emptyMap());
    }

    /**
     * Constructor for Group which accepts the name of the group and metadata.
     * This uses the default namespace.
     *
     * @param name      the name of the group.
     * @param metadata  the metadata assigned to the group.
     */
    public Group(@NonNull String name, Map<String, Object> metadata) {
        this(DEFAULT_NAMESPACE, name, metadata);
    }

    /**
     * Constructor for Group which accepts both the fully qualified name of
     * the group and metadata associated with the group. Once assigned,
     * metadata cannot be added or removed without creating a new record.
     *
     * @param namespace the namespace of the group.
     * @param name      the name of the group.
     * @param metadata  the metadata assigned to the group.
     */
    public Group(@NonNull String namespace, @NonNull String name, Map<String, Object> metadata) {
        this.namespace = namespace;
        this.name = name.trim();
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) :
                Collections.emptyMap();
        var nameMatcher = VALID_NAME_PATTERN.matcher(this.name);
        if (!nameMatcher.matches()) {
            throw new IllegalArgumentException("Group name contains invalid characters.");
        }
    }

    /**
     * Custom implementation of the equals function. It is recommended to
     * review the documentation of {@link Object#equals(Object)} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, equality can be determined by the
     * fully qualified name of the group alone.
     * </p>
     *
     * @param o the reference object with which to compare against this
     *          object.
     * @return if this object equals the provided object per the standard
     * contract for {@link Object#equals(Object)}.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Group group &&
                Objects.equals(namespace, group.namespace) &&
                Objects.equals(name, group.name);
    }

    /**
     * Custom implementation of the hashCode function. It is recommended to
     * review the documentation of {@link Object#hashCode()} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, hashCode can be computed by the
     * fully qualified name of the group alone.
     * </p>
     *
     * @return the hashCode of this record based on the standard contract for
     * {@link Object#hashCode()}.
     */
    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }

    /**
     * Returns a string representation of this group. It is recommended to
     * review the documentation of {@link Record#toString()} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, toString can be computed by the
     * fully qualified name of the group.
     * </p>
     *
     * @return a string representation of the group.
     */
    @Override
    public String toString() {
        return namespace + "-" + name;
    }
}
