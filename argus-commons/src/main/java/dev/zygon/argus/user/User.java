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
 * @param uuid the unique ID associated with the user - the origin of which
 *             may come from outside the Argus platform.
 * @param name the name of the user which is used for the purposes of display
 *             to other users.
 * @param metadata any metadata associated with this user which may be useful.
 */
@Builder
public record User(@NonNull String uuid, @NonNull String name, @NonNull Map<String, Object> metadata) {

    public User(@NonNull String uuid, @NonNull String name) {
        this(uuid, name, Collections.emptyMap());
    }

    public User(@NonNull String uuid, @NonNull String name, @NonNull Map<String, Object> metadata) {
        this.uuid = uuid;
        this.name = name;
        this.metadata = Collections.unmodifiableMap(metadata);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User user &&
                Objects.equals(uuid, user.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
