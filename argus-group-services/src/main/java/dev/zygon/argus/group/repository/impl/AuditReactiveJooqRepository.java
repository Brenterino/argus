package dev.zygon.argus.group.repository.impl;

import dev.zygon.argus.group.audit.Audit;
import dev.zygon.argus.group.audit.AuditLog;
import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.repository.AuditRepository;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

@Slf4j
@ApplicationScoped
public class AuditReactiveJooqRepository implements AuditRepository {

    @Override
    public Uni<AuditLog> forGroup(Group group) {
        return null;
    }

    @Override
    public Uni<Boolean> create(Group group, Audit audit) {
        return Uni.createFrom()
                .item(true);
    }

    @Override
    public Uni<Boolean> delete(Group group) {
        return Uni.createFrom()
                .item(true);
    }
}
