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
package dev.zygon.argus.client.status;

import dev.zygon.argus.client.ArgusClient;
import dev.zygon.argus.status.UserStatus;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public enum StatusStorage {

    INSTANCE;

    // TODO not sure if there's a really clean way to clean up data, maybe we can use a cache and evict?

    @Getter
    private final Map<UUID, UserStatus> storage;

    StatusStorage() {
        storage = new ConcurrentHashMap<>();
    }

    public void fromRemote(UserStatus status) {
        storage.put(status.source(), status);
    }

    public void syncRemote(ArgusClient client) {
        var status = UserStatusChecker.INSTANCE.getUserStatus();
        if (status != null) {
            client.getStatuses().send(status);
        }
    }
}
