package dev.zygon.argus.client.api;

import dev.zygon.argus.permission.GroupPermissions;
import dev.zygon.argus.permission.UserPermission;
import dev.zygon.argus.permission.UserPermissions;
import dev.zygon.argus.user.User;
import retrofit2.Call;
import retrofit2.http.*;

public interface ArgusPermissionApi {

    @GET("/permissions")
    Call<GroupPermissions> elected();

    @PUT("/permissions/{groupName}")
    Call<Void> elect(@Path("groupName") String group,
                     @Body UserPermission permission);

    @GET("/permissions/{groupName}/admin")
    Call<UserPermissions> members(@Path("groupName") String group,
                                  @Query("page") int page,
                                  @Query("size") int size);

    @POST("/permissions/{groupName}/admin")
    Call<Void> invite(@Path("groupName") String group,
                      @Body UserPermission permission);

    @PUT("/permissions/{groupName}/admin")
    Call<Void> modify(@Path("groupName") String group,
                      @Body UserPermission permission);

    @DELETE("/permissions/{groupName}/admin")
    Call<Void> kick(@Path("groupName") String group,
                    @Body User target);
}