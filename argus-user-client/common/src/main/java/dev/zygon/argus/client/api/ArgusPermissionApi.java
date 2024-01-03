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

import dev.zygon.argus.permission.GroupPermissions;
import dev.zygon.argus.permission.UserPermission;
import dev.zygon.argus.permission.UserPermissions;
import dev.zygon.argus.user.User;
import retrofit2.Call;
import retrofit2.http.*;

public interface ArgusPermissionApi {

    @GET("/groups/permissions")
    Call<GroupPermissions> elected();

    @PUT("/groups/permissions/{groupName}")
    Call<Void> elect(@Path("groupName") String group,
                     @Body UserPermission permission);

    @GET("/groups/permissions/{groupName}/admin")
    Call<UserPermissions> members(@Path("groupName") String group,
                                  @Query("page") int page,
                                  @Query("size") int size);

    @POST("/groups/permissions/{groupName}/admin")
    Call<Void> invite(@Path("groupName") String group,
                      @Body UserPermission permission);

    @PUT("/groups/permissions/{groupName}/admin")
    Call<Void> modify(@Path("groupName") String group,
                      @Body UserPermission permission);

    @DELETE("/groups/permissions/{groupName}/admin")
    Call<Void> kick(@Path("groupName") String group,
                    @Body User target);
}
