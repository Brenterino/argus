package dev.zygon.argus.group.repository.impl;

import dev.zygon.argus.group.exception.FatalGroupException;
import dev.zygon.argus.group.repository.NamespaceRepository;
import dev.zygon.argus.namespace.Namespace;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.jooq.generated.Tables.NAMESPACES;
import static org.jooq.generated.Tables.NAMESPACE_MAPPINGS;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.using;

@Slf4j
@ApplicationScoped
public class NamespaceReactiveJooqRepository implements NamespaceRepository {

    private final Pool pool;
    private final Configuration configuration;
    private final Map<String, String> queryCache;

    public NamespaceReactiveJooqRepository(Pool pool, Configuration configuration) {
        this.pool = pool;
        this.configuration = configuration;
        this.queryCache = new HashMap<>();
    }

    @Override
    public Uni<Optional<Namespace>> matching(String mapping) {
        final var MATCHING_NAMESPACE = "MATCHING_NAMESPACE";
        var matchingNamespaceSql = queryCache.computeIfAbsent(MATCHING_NAMESPACE,
                k -> renderMatchingNamespaceSql());
        if (log.isDebugEnabled()) {
            log.debug("Operation(Find Matching Namespace) SQL: {}", matchingNamespaceSql);
            log.debug("Operation(Find Matching Namespace) Params: mapping({})", mapping);
        }
        return pool.preparedQuery(matchingNamespaceSql)
                .execute(Tuple.of(mapping))
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .collect().first()
                .map(Optional::ofNullable)
                .map(row -> row.map(RowMappers::namespace))
                .onFailure()
                .transform(e -> new FatalGroupException("Retrieving namespace for mapping unexpectedly failed.", e));
    }

    private String renderMatchingNamespaceSql() {
        var mappingQuery = using(configuration)
                .select(NAMESPACE_MAPPINGS.NAMESPACE_ID)
                .from(NAMESPACE_MAPPINGS)
                .where(NAMESPACE_MAPPINGS.MAPPING.eq(field("$1", String.class)));
        return using(configuration)
                .select(NAMESPACES.NAME)
                .from(NAMESPACES)
                .where(NAMESPACES.ID.eq(mappingQuery))
                .getSQL();
    }
}
