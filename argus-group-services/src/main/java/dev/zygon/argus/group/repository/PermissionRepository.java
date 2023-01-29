/*
    Argus - Suite of services aimed to enhance Minecraft Multiplayer
    Copyright (C) 2023 Zygon

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
