package dev.zygon.argus.group.repository;

import dev.zygon.argus.group.audit.Audit;
import dev.zygon.argus.group.audit.AuditLog;
import dev.zygon.argus.group.Group;
import io.smallrye.mutiny.Uni;

public interface AuditRepository {

    Uni<AuditLog> forGroup(Group group);

    Uni<Boolean> create(Group group, Audit audit);

    Uni<Boolean> delete(Group group);
}
