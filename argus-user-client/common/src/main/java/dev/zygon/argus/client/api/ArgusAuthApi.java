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
package dev.zygon.argus.client.api;

import dev.zygon.argus.auth.ArgusToken;
import dev.zygon.argus.auth.DualToken;
import dev.zygon.argus.auth.MojangAuthData;
import dev.zygon.argus.auth.OneTimePassword;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Tag;

public interface ArgusAuthApi {

    @GET("/auth/key")
    Call<String> publicKey();

    @POST("/auth/mojang")
    Call<DualToken> authMojang(@Body MojangAuthData authData);

    @POST("/auth/refresh")
    Call<ArgusToken> refresh(@Tag ArgusToken refreshToken);

    @GET("/auth/otp")
    Call<OneTimePassword> generateOTP();
}
