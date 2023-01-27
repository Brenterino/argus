package dev.zygon.argus.client.api;

import dev.zygon.argus.auth.ArgusToken;
import dev.zygon.argus.auth.MojangAuthData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ArgusAuthApi {

    @GET("/auth/key")
    Call<String> publicKey();

    @POST("/auth/mojang")
    Call<ArgusToken> authMojang(@Body MojangAuthData authData);
}
