package dev.zygon.argus.user;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.Permission;
import lombok.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public record Permissions(@NonNull Map<Group, Permission> permissions) {

    public Permissions(Map<Group, Permission> permissions) {
        this.permissions = Collections.unmodifiableMap(permissions);
    }

    public Set<Group> readGroups() {
        return groups(Permission::canRead);
    }

    public Set<Group> writeGroups() {
        return groups(Permission::canWrite);
    }

    public Set<Group> adminGroups() {
        return groups(p -> p == Permission.ADMIN);
    }

    public Set<Group> groups(Predicate<Permission> predicate) {
        return permissions.entrySet()
                .stream()
                .filter(v -> predicate.test(v.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static final String GROUPS_SEPARATOR = "-";

    public static Permissions fromRaw(Set<String> raw) {
        var permissions = raw.stream()
                .map(group -> group.split(GROUPS_SEPARATOR))
                .map(PermissionTuple::new)
                .collect(Collectors.toMap(PermissionTuple::group, PermissionTuple::permission));

        return new Permissions(permissions);
    }

    public Set<String> toRaw() {
        return permissions.entrySet()
                .stream()
                .map(PermissionTuple::new)
                .map(PermissionTuple::toRaw)
                .collect(Collectors.toUnmodifiableSet());
    }

    private record PermissionTuple(Group group, Permission permission) {

        public PermissionTuple(Map.Entry<Group, Permission> permission) {
            this(permission.getKey(), permission.getValue());
        }

        public PermissionTuple(String[] raw) {
            this(new Group(raw[0]), Permission.valueOf(raw[1]));
        }

        public String toRaw() {
            return group + GROUPS_SEPARATOR + permission;
        }
    }
}
