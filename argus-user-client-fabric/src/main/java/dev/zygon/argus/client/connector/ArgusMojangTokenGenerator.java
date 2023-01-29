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
package dev.zygon.argus.client.connector;

import dev.zygon.argus.auth.ArgusToken;
import dev.zygon.argus.auth.MojangAuthData;
import dev.zygon.argus.client.api.ArgusAuthApi;
import dev.zygon.argus.client.auth.RefreshableTokenGenerator;
import dev.zygon.argus.client.config.ArgusClientConfig;
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
import java.util.concurrent.Future;

@Slf4j
public enum ArgusMojangTokenGenerator implements RefreshableTokenGenerator {

    INSTANCE;

    @Setter private ArgusToken token;
    @Setter private String server;
    @Setter private String username;
    @Setter private ArgusAuthApi auth;

    @Override
    public boolean isExpired() {
        var config = ArgusClientConfig.getActiveConfig();
        var now = Instant.now(Clock.systemUTC());
        return token == null || token.expiration()
                .minus(config.getRefreshTokenRenewBeforeExpirationSeconds(), ChronoUnit.SECONDS)
                .isAfter(now);
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
        // maintain token in case of immediate need for another API request
        // after one that requires a force refresh
        token = new ArgusToken(token.token(), Instant.now());
    }

    private void retrieveToken() {
        if (isExpired()) {
            ClientScheduler.INSTANCE
                    .invoke(MojangAuthConnector.INSTANCE::connectMojang)
                    .addListener(this::performArgusAuth);
        }
    }

    @SneakyThrows
    private void performArgusAuth(Future<?> hashFuture) {
        var hash = (String) hashFuture.get();
        var authData = new MojangAuthData(server, username, hash);
        var authCall = auth.authMojang(authData);
        authCall.enqueue(new TokenCallback());
    }

    @EverythingIsNonNull
    private class TokenCallback implements Callback<ArgusToken> {

        @Override
        public void onResponse(Call<ArgusToken> call, Response<ArgusToken> response) {
            if (response.isSuccessful()) {
                setToken(response.body());
            } else {
                log.warn("[ARGUS] Retrieval of token failed.");
            }
        }

        @Override
        public void onFailure(Call<ArgusToken> call, Throwable cause) {
            log.warn("[ARGUS] Retrieval of token failed. Either auth service is down or client could not be verified.", cause);
        }
    }

    @Override
    public String token() {
        return token != null ? token.token() : null;
    }
}
