package dev.zygon.argus.auth.repository.impl;

import dev.zygon.argus.auth.ArgusToken;
import dev.zygon.argus.auth.MojangAuthData;
import dev.zygon.argus.auth.MojangAuthStatus;
import dev.zygon.argus.auth.repository.ArgusTokenIssueRepository;
import dev.zygon.argus.auth.service.ArgusTokenGenerator;
import dev.zygon.argus.permission.Permissions;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;

@ApplicationScoped
public class ArgusGeneratedTokenIssueRepository implements ArgusTokenIssueRepository {

    private final ArgusTokenGenerator generator;

    public ArgusGeneratedTokenIssueRepository(ArgusTokenGenerator generator) {
        this.generator = generator;
    }

    @Override
    public Uni<ArgusToken> issue(MojangAuthData data, MojangAuthStatus status) {
        var emptyPermissions = new Permissions(Collections.emptyMap());
        return Uni.createFrom()
                .item(generator.generate(status.uuid(), data.server(), emptyPermissions));
    }
}
