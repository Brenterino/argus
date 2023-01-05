package dev.zygon.argus.group.repository.impl;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.group.audit.Audit;
import dev.zygon.argus.group.audit.AuditLog;
import dev.zygon.argus.group.exception.FatalGroupException;
import dev.zygon.argus.group.repository.AuditRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;

import javax.enterprise.context.ApplicationScoped;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.zygon.argus.group.repository.impl.ColumnNames.COUNT_NAME;
import static dev.zygon.argus.group.repository.impl.ColumnNames.PERMISSION_NAME;
import static dev.zygon.argus.group.repository.impl.CommonJooqRenderer.groupSelect;
import static org.jooq.generated.Tables.GROUP_AUDIT;
import static org.jooq.impl.DSL.*;

@Slf4j
@ApplicationScoped
public class AuditReactiveJooqRepository implements AuditRepository {

    private final Pool pool;
    private final Configuration configuration;
    private final Map<String, String> queryCache;

    public AuditReactiveJooqRepository(Pool pool, Configuration configuration) {
        this.pool = pool;
        this.configuration = configuration;
        this.queryCache = new HashMap<>();
    }

    @Override
    public Uni<AuditLog> forGroup(Group group, int page, int size) {
        final var FOR_GROUP_QUERY = "AUDIT_FOR_GROUP";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var offset = page * size;
        var forGroupSql = queryCache.computeIfAbsent(FOR_GROUP_QUERY,
                k -> renderForGroupSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Audit For Group) SQL: {}", forGroupSql);
            log.debug("Operation(Audit For Group) Params: namespace({}), group({}), page({}), size({})",
                    namespace, name, page, size);
        }
        return pool.preparedQuery(forGroupSql)
                .execute(Tuple.of(namespace, name, offset, size))
                .map(rows -> RowMappers.auditLog(rows, size))
                .onFailure()
                .transform(e -> new FatalGroupException("Loading audit log unexpectedly failed.", e));
    }

    private String renderForGroupSql() {
        var recordQuery = using(configuration)
                .select(
                        GROUP_AUDIT.CHANGER, GROUP_AUDIT.TARGET,
                        GROUP_AUDIT.ACT, GROUP_AUDIT.PERMISSION.as(PERMISSION_NAME),
                        GROUP_AUDIT.OCCURRED, inline(-1).as(COUNT_NAME))
                .from(GROUP_AUDIT)
                .where(GROUP_AUDIT.GROUP_ID
                        .eq(groupSelect(configuration)))
                .orderBy(GROUP_AUDIT.ID.desc())
                .offset(field("$3", Integer.class))
                .limit(field("$4", Integer.class));
        var countQuery = using(configuration)
                .select(
                        inline(null, UUID.class).as(GROUP_AUDIT.CHANGER),
                        inline(null, UUID.class).as(GROUP_AUDIT.TARGET),
                        inline(null, Integer.class).as(GROUP_AUDIT.ACT),
                        inline(null, Integer.class).as(PERMISSION_NAME),
                        inline(null, OffsetDateTime.class).as(GROUP_AUDIT.OCCURRED),
                        count().as(COUNT_NAME))
                .from(GROUP_AUDIT)
                .where(GROUP_AUDIT.GROUP_ID
                        .eq(groupSelect(configuration)));
        return recordQuery.unionAll(countQuery)
                .getSQL();
    }

    @Override
    public Uni<Boolean> create(Group group, Audit audit) {
        final var CREATE_AUDIT_QUERY = "CREATE_AUDIT";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var changer = audit.changer();
        var target = audit.target();
        var act = audit.action();
        var permission = audit.permission();
        var occurred = audit.occurred();
        var createAuditSql = queryCache.computeIfAbsent(CREATE_AUDIT_QUERY,
                k -> renderCreateAuditSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Create Audit) SQL: {}", createAuditSql);
            log.debug("Operation(Create Audit) Params: namespace({}), group({}), audit({})",
                    namespace, name, audit);
        }
        return pool.preparedQuery(createAuditSql)
                .execute(Tuple.of(namespace, name,
                                changer, target,
                                act.ordinal(), permission.ordinal())
                        .addOffsetDateTime(occurred))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> true)
                .onFailure()
                .transform(e -> new FatalGroupException("Creating audit record unexpectedly failed.", e));
    }

    private String renderCreateAuditSql() {
        return using(configuration)
                .insertInto(GROUP_AUDIT,
                        GROUP_AUDIT.GROUP_ID,
                        GROUP_AUDIT.CHANGER, GROUP_AUDIT.TARGET,
                        GROUP_AUDIT.ACT, GROUP_AUDIT.PERMISSION,
                        GROUP_AUDIT.OCCURRED)
                .values(
                        field(groupSelect(configuration)),
                        field("$3", UUID.class), field("$4", UUID.class),
                        field("$5", Integer.class), field("$6", Integer.class),
                        field("$7", OffsetDateTime.class)
                ).getSQL();
    }

    @Override
    public Uni<Boolean> delete(Group group) {
        final var DELETE_AUDIT_QUERY = "DELETE_AUDIT";
        var namespace = group.namespace();
        var name = group.name().toLowerCase();
        var deleteAuditSql = queryCache.computeIfAbsent(DELETE_AUDIT_QUERY,
                k -> renderDeleteAuditSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Delete Audit) SQL: {}", deleteAuditSql);
            log.debug("Operation(Delete Audit) Params: namespace({}), group({})",
                    namespace, name);
        }
        return pool.preparedQuery(deleteAuditSql)
                .execute(Tuple.of(namespace, name))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .collect().first()
                .map(row -> true)
                .onFailure()
                .transform(e -> new FatalGroupException("Deleting audit records unexpectedly failed.", e));
    }

    private String renderDeleteAuditSql() {
        return using(configuration)
                .deleteFrom(GROUP_AUDIT)
                .where(GROUP_AUDIT.GROUP_ID
                        .eq(groupSelect(configuration)))
                .getSQL();
    }
}
