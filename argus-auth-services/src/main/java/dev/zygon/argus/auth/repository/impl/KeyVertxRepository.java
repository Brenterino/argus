package dev.zygon.argus.auth.repository.impl;

import dev.zygon.argus.auth.configuration.AuthConfiguration;
import dev.zygon.argus.auth.repository.KeyRepository;
import io.smallrye.mutiny.Uni;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.file.AsyncFile;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeyVertxRepository implements KeyRepository {

    private final Vertx vertx;
    private final AuthConfiguration configuration;

    public KeyVertxRepository(Vertx vertx,
                              AuthConfiguration configuration) {
        this.vertx = vertx;
        this.configuration = configuration;
    }

    @Override
    public Uni<AsyncFile> publicKey() {
        var openOptions = new OpenOptions()
                .setCreate(false)
                .setWrite(false);
        return vertx.fileSystem()
                .open(configuration.publicKey(), openOptions);
    }
}
