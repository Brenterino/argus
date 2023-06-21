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
package dev.zygon.argus.client.groups;

import dev.zygon.argus.client.api.ArgusGroupApi;
import dev.zygon.argus.client.api.ArgusPermissionApi;
import dev.zygon.argus.permission.GroupPermission;
import dev.zygon.argus.permission.GroupPermissions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public enum GroupStorage {

    INSTANCE;

    @Setter private ArgusGroupApi groups;
    @Setter private ArgusPermissionApi permissions;

    @Setter @Getter private Set<GroupPermission> membership;
    @Setter @Getter private Set<GroupPermission> elections;

    public void refreshMemberships() {
        // TODO force close of locations sockets if membership changes?
        doCall(groups.groups(), this::setMembership);
    }

    public void refreshElections() {
        // TODO force close of locations sockets if elections change?
        doCall(permissions.elected(), this::setElections);
    }

    private void doCall(Call<GroupPermissions> apiCall, Consumer<Set<GroupPermission>> setter) {
        try {
            Optional.of(apiCall.execute())
                    .map(Response::body)
                    .map(GroupPermissions::permissions)
                    .ifPresent(setter);
        } catch (Exception e) {
            log.warn("[ARGUS] Retrieval of group membership failed!", e);
        }
    }

    public void clean() {
        membership = null;
        elections = null;
    }
}
