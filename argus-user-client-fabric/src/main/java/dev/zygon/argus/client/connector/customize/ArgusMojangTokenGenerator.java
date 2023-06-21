/*
    Argus - Suite of services aimed to enhance Minecraft Multiplayer
    Copyright (C) 2023 Zygon

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.zygon.argus.client.connector.customize;

import dev.zygon.argus.auth.ArgusToken;
import dev.zygon.argus.auth.DualToken;
import dev.zygon.argus.auth.MojangAuthData;
import dev.zygon.argus.client.api.ArgusAuthApi;
import dev.zygon.argus.client.auth.RefreshableTokenGenerator;
import dev.zygon.argus.client.config.ArgusClientConfig;
import dev.zygon.argus.client.connector.MojangAuthConnector;
import dev.zygon.argus.client.scheduler.ClientScheduler;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Function;

@Slf4j
public enum ArgusMojangTokenGenerator implements RefreshableTokenGenerator {

    INSTANCE;

    private DualToken token;
    @Setter private String server;
    @Setter private String username;
    @Setter private ArgusAuthApi auth;

    @Override
    public boolean isRefreshTokenExpired() {
        return checkToken(DualToken::refreshToken);
    }

    @Override
    public boolean isAccessTokenExpired() {
        return checkToken(DualToken::accessToken);
    }

    private boolean checkToken(Function<DualToken, ArgusToken> tokenFromDual) {
        var config = ArgusClientConfig.getActiveConfig();
        var now = Instant.now(Clock.systemUTC());
        return Optional.ofNullable(token)
                .map(tokenFromDual)
                .map(ArgusToken::expiration)
                .map(expiration -> expiration.minus(config.getRefreshTokenRenewBeforeExpirationSeconds(), ChronoUnit.SECONDS))
                .map(now::isAfter)
                .orElse(true);
    }

    @Override
    public void refresh() {
        if (auth != null) {
            retrieveToken();
        } else {
            log.warn("[ARGUS] Tried to invoke token refresh even though API connector has not been provided yet.");
        }
    }

    @Override
    public void forceRefresh() {
        // maintain tokens in case of immediate need for another API request
        // after one that requires a force refresh
        var now = Instant.now();
        var refreshToken = new ArgusToken(token.refreshToken()
                .token(), now);
        var accessToken = new ArgusToken(token.accessToken()
                .token(), now);
        token = new DualToken(refreshToken, accessToken);
    }

    private void retrieveToken() {
        if (isRefreshTokenExpired()) { // access token will also be refreshed
            ClientScheduler.INSTANCE
                    .invoke(MojangAuthConnector.INSTANCE::connectMojang)
                    .addListener(this::performMojangAuth);
        } else if (isAccessTokenExpired()) {
            performRefresh();
        }
    }

    @SneakyThrows
    private void performMojangAuth(Future<?> hashFuture) {
        var hash = (String) hashFuture.get();
        var authData = new MojangAuthData(server, username, hash);
        var authCall = auth.authMojang(authData);
        authCall.enqueue(new TokenCallback<>());
    }

    private void performRefresh() {
        var refreshCall = auth.refresh(token.refreshToken());
        refreshCall.enqueue(new TokenCallback<>());
    }

    @EverythingIsNonNull
    private class TokenCallback<E> implements Callback<E> {

        @Override
        public void onResponse(Call<E> call, Response<E> response) {
            if (response.isSuccessful()) {
                setToken(response.body());
            } else {
                log.warn("[ARGUS] Retrieval of token failed.");
            }
        }

        private void setToken(E response) {
            if (response instanceof DualToken mojangToken) {
                token = mojangToken;
            } else if (response instanceof ArgusToken accessToken) {
                token = new DualToken(token.refreshToken(), accessToken);
            } else {
                log.warn("[ARGUS] Received response via callback that was not of any expected type.");
            }
        }

        @Override
        public void onFailure(Call<E> call, Throwable cause) {
            log.warn("[ARGUS] Retrieval of token failed. Either auth service is down or client could not be verified.");
        }
    }

    @Override
    public String token() {
        return Optional.ofNullable(token)
                .map(DualToken::accessToken)
                .map(ArgusToken::token)
                .orElse(null);
    }

    public void clean() {
        this.username = null;
        this.server = null;
        this.token = null;
    }
}
