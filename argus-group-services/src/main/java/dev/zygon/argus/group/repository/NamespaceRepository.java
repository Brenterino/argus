package dev.zygon.argus.group.repository;

import dev.zygon.argus.namespace.Namespace;
import io.smallrye.mutiny.Uni;

import java.util.Optional;

public interface NamespaceRepository {

    Uni<Optional<Namespace>> matching(String mapping);
}
