package dev.zygon.argus.group.repository;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.audit.Audit;
import dev.zygon.argus.group.audit.AuditLog;
import io.smallrye.mutiny.Uni;

public interface AuditRepository {

    Uni<AuditLog> forGroup(Group group, int page, int size);

    Uni<Boolean> create(Group group, Audit audit);

    Uni<Boolean> delete(Group group);
}
