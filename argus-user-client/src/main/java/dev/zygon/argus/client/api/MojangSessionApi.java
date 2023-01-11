package dev.zygon.argus.client.api;

import dev.zygon.argus.client.domain.ServerJoinData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;

public interface MojangSessionApi {

    @GET("/session/minecraft/join")
    Call<Void> join(@Body ServerJoinData data);
}
