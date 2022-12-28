package dev.zygon.argus.group.repository;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.Groups;
import dev.zygon.argus.group.exception.GroupException;
import dev.zygon.argus.user.NamespaceUser;
import io.smallrye.mutiny.Uni;

public interface GroupRepository {

    Uni<Groups> ownedBy(NamespaceUser user) throws GroupException;

    Uni<Boolean> ownedBy(Group group, NamespaceUser user) throws GroupException;

    Uni<Boolean> exists(Group group) throws GroupException;

    Uni<Boolean> create(Group group, NamespaceUser creator) throws GroupException;

    Uni<Boolean> update(Group group) throws GroupException;

    Uni<Boolean> delete(Group group) throws GroupException;
}
