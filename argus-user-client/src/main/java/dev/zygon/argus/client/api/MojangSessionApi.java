package dev.zygon.argus.client.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MojangSessionApi {

    @GET("/session/minecraft/join")
    Call<Void> join();
}
