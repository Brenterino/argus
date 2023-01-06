package dev.zygon.argus.group.repository;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.permission.GroupPermissions;
import dev.zygon.argus.permission.Permission;
import dev.zygon.argus.permission.UserPermissions;
import dev.zygon.argus.user.NamespaceUser;
import io.smallrye.mutiny.Uni;

public interface PermissionRepository {

    Uni<UserPermissions> forGroup(Group group, int page, int size);

    Uni<Boolean> grant(Group group, NamespaceUser namespaceUser, Permission permission);

    Uni<Boolean> remove(Group group, NamespaceUser namespaceUser);

    Uni<Boolean> hasPermission(Group group, NamespaceUser namespaceUser, Permission permission);

    Uni<GroupPermissions> available(NamespaceUser namespaceUser);

    Uni<GroupPermissions> elected(NamespaceUser namespaceUser);

    Uni<Boolean> elect(Group group, NamespaceUser namespaceUser, Permission permission);
}
