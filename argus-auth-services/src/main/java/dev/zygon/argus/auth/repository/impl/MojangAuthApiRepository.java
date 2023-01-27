package dev.zygon.argus.auth.repository.impl;

import dev.zygon.argus.auth.MojangAuthData;
import dev.zygon.argus.auth.MojangAuthStatus;
import dev.zygon.argus.auth.repository.MojangAuthRepository;
import dev.zygon.argus.auth.service.MojangAuthService;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MojangAuthApiRepository implements MojangAuthRepository {

    private final MojangAuthService authService;

    public MojangAuthApiRepository(@RestClient MojangAuthService authService) {
        this.authService = authService;
    }

    @Override
    public Uni<MojangAuthStatus> status(MojangAuthData authData) {
        return authService.authorize(authData.username(), authData.hash());
    }
}
