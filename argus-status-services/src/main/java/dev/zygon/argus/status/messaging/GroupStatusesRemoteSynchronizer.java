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
package dev.zygon.argus.status.messaging;

import io.smallrye.mutiny.Multi;
import io.vertx.core.json.JsonObject;

/**
 * Abstraction for synchronizer which can send and receive status data
 * to/from remote instances for the purposes of synchronizing this data
 * across all instances for distribution to clients.
 */
public interface GroupStatusesRemoteSynchronizer {

    /**
     * Method which is invoked whenever a message is received from a remote
     * instance. Will come in the form of a raw JSON message and must be
     * converted into the appropriate message.
     *
     * @param rawMessage the raw JSON message which was received from a remote
     *                   instance sending data.
     */
    void receive(JsonObject rawMessage);

    /**
     * Creates a {@link Multi} which is used to publish data from this instance
     * to remote instances for the purposes of synchronization.
     *
     * @return a {@link Multi} which is used to publish status data out to
     * other instances.
     */
    Multi<UserStatusesMessage> send();
}
