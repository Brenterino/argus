package dev.zygon.argus.permission;

import dev.zygon.argus.group.Group;
import lombok.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Record which represents permissions available to a user on a group basis.
 *
 * @param permissions the mapping of group to permission. There can only be
 *                    one permission for each group and this permission cannot
 *                    be mutated from this structure.
 */
public record Permissions(@NonNull Map<Group, Permission> permissions) {

    /**
     * Separator character which separates the group and permission in the raw
     * form of the permission entry.
     */
    private static final String GROUPS_SEPARATOR = "-";

    /**
     * Constructor for Permissions which accepts a mapping of group to
     * permission which are available for a user.
     *
     * @param permissions the raw mapping of group to permission.
     */
    public Permissions(Map<Group, Permission> permissions) {
        this.permissions = permissions != null ? Collections.unmodifiableMap(permissions) :
                Collections.emptyMap();
    }

    /**
     * Converts from the raw form of permission entries into the record form.
     * Running {@link Permissions#toRaw()} is the inverse operation and will
     * result in a set that is the same as the input set.
     *
     * @param raw a set of raw group-permission values that will be translated
     *            into individual entries.
     * @return a record which contains the record form of group-permission
     * values.
     */
    public static Permissions fromRaw(Set<String> raw) {
        var permissions = raw.stream()
                .map(group -> group.split(GROUPS_SEPARATOR))
                .filter(split -> split.length == 3)
                .map(PermissionTuple::new)
                .collect(Collectors.toMap(PermissionTuple::group, PermissionTuple::permission));
        return new Permissions(permissions);
    }

    /**
     * Extract the groups where the permission allows for read access.
     *
     * @return groups which are assigned in this Permissions record that allow
     * for read access.
     * @see Permission#canRead() this is the test predicate used as an argument
     * to {@link Permissions#groups(Predicate)}
     */
    public Set<Group> readGroups() {
        return groups(Permission::canRead);
    }

    /**
     * Extract the groups where the permission allows for write access.
     *
     * @return groups which are assigned in this Permissions record that allow
     * for write access.
     * @see Permission#canWrite() this is the test predicate used as an argument
     * to {@link Permissions#groups(Predicate)}
     */
    public Set<Group> writeGroups() {
        return groups(Permission::canWrite);
    }

    /**
     * Extract the groups where the permission allows for admin access.
     *
     * @return groups which are assigned in this Permissions record that allow
     * for admin access.
     * @see Permission#ADMIN this is the value used to test against for the
     * predicate used as an argument to
     * {@link Permissions#groups(Predicate)}
     */
    public Set<Group> adminGroups() {
        return groups(p -> p == Permission.ADMIN);
    }

    /**
     * Retrieve groups which have permissions that match a specified predicate.
     *
     * @param predicate test predicate which can be used to find groups that
     *                  have a desired permission.
     * @return the groups which have a permission which pass the provided test
     * predicate.
     */
    public Set<Group> groups(Predicate<Permission> predicate) {
        return permissions.entrySet()
                .stream()
                .filter(v -> predicate.test(v.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Converts from the record form of the permission entries into the raw
     * form. Running {@link Permissions#fromRaw(Set)} is the inverse operation
     * and will result in a set that is the same as the input set.
     *
     * @return a set of the raw version of the permissions in this Permissions
     * record.
     */
    public Set<String> toRaw() {
        return permissions.entrySet()
                .stream()
                .map(PermissionTuple::new)
                .map(PermissionTuple::toRaw)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Private record which is used for computation between raw and record
     * forms of group-permission combinations. Can be created from a raw
     * form of a split string or from the record form of Group and Permission
     * records.
     *
     * @param group      group record of the tuple.
     * @param permission permission record of the tuple.
     */
    private record PermissionTuple(Group group, Permission permission) {

        public PermissionTuple(Map.Entry<Group, Permission> permission) {
            this(permission.getKey(), permission.getValue());
        }

        public PermissionTuple(String[] raw) {
            this(new Group(raw[0], raw[1]), Permission.valueOf(raw[2]));
        }

        public String toRaw() {
            return group.namespace() + GROUPS_SEPARATOR + group.name() + GROUPS_SEPARATOR + permission;
        }
    }
}
