package dev.zygon.argus.client.api;

import dev.zygon.argus.group.audit.AuditLog;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ArgusAuditApi {

    @GET("/audits/{groupName}")
    Call<AuditLog> audit(@Path("groupName") String group,
                         @Query("page") int page,
                         @Query("size") int size);
}
