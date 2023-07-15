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
package dev.zygon.argus.status;

import dev.zygon.argus.status.client.UserStatusClients;
import io.quarkus.test.common.http.TestHTTPResource;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Disabled("Only meant to be used as a base test and should not run directly.")
public class UserStatusSocketIT {

    @TestHTTPResource("/statuses")
    private URI uri;

    private Session alice;
    private Session bob;

    @BeforeEach
    public void setUp() throws Exception {
        alice = ContainerProvider.getWebSocketContainer()
                .connectToServer(UserStatusClients.AliceClient.class, uri);
        bob = ContainerProvider.getWebSocketContainer()
                .connectToServer(UserStatusClients.BobClient.class, uri);
        UserStatusClients.ALICE_RECEIVED.clear();
        UserStatusClients.BOB_RECEIVED.clear();
    }

    @Test
    public void testStatusMessage() throws Exception {
        var aliceUUID = UUID.fromString("3b8274a9-aa64-47bc-89a7-64cf896aae93");
        var bobUUID = UUID.fromString("b3c53ad9-cbd9-4644-8371-c2468d8a0e57");
        var statusAlice = new UserStatus(aliceUUID, 10.0f, List.of(), List.of());
        var statusBob = new UserStatus(bobUUID, 5.0f, List.of(), List.of());

        alice.getAsyncRemote()
                .sendObject(statusAlice);
        bob.getAsyncRemote()
                .sendObject(statusBob);

        Thread.sleep(1000);

        var allAliceStatuses = UserStatusClients.ALICE_RECEIVED.stream()
                .toList();
        var allBobStatuses = UserStatusClients.BOB_RECEIVED.stream()
                .toList();

        assertThat(allAliceStatuses)
                .isNotNull()
                .contains(statusBob, statusAlice);
        assertThat(allBobStatuses)
                .isNotNull()
                .contains(statusBob);
    }

    @AfterEach
    public void tearDown() throws Exception {
        alice.close();
        bob.close();
        UserStatusClients.ALICE_RECEIVED.clear();
        UserStatusClients.BOB_RECEIVED.clear();
    }
}
