package dev.zygon.argus.auth.repository;

import dev.zygon.argus.auth.ArgusToken;
import dev.zygon.argus.auth.MojangAuthData;
import dev.zygon.argus.auth.MojangAuthStatus;
import io.smallrye.mutiny.Uni;

public interface ArgusTokenIssueRepository {

    Uni<ArgusToken> issue(MojangAuthData data, MojangAuthStatus status);
}
