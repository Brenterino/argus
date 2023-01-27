package dev.zygon.argus.auth.repository;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.file.AsyncFile;

public interface KeyRepository {

    Uni<AsyncFile> publicKey();
}
