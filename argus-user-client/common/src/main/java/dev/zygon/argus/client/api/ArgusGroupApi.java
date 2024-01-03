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
