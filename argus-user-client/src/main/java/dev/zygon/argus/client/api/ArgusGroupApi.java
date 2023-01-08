package dev.zygon.argus.client.api;

import dev.zygon.argus.permission.GroupPermissions;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface ArgusGroupApi {

    @GET("/groups")
    Call<GroupPermissions> groups();

    @DELETE("/groups/{groupName}")
    Call<Void> leave(@Path("groupName") String group);

    @POST("/groups/{groupName}/admin")
    Call<Void> create(@Path("groupName") String groupName,
                      @Body Map<String, Object> metadata);

    @POST("/groups/{groupName}/admin")
    Call<Void> update(@Path("groupName") String groupName,
                      @Body Map<String, Object> metadata);

    @DELETE("/groups/{groupName}/admin")
    Call<Void> delete(@Path("groupName") String groupName);
}
