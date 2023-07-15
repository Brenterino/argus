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
import dev.zygon.argus.status.profile.IntegrationProfile;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@QuarkusTest
@TestProfile(IntegrationProfile.class)
public class UserStatusSocketJwtExpirationIT {

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
    }

    @Test
    public void sessionCannotBeOpenedIfJwtIsExpired() {
        setMaxStackTraceElementsDisplayed(1000);
        assertThatThrownBy(() -> {
            try (var expired = ContainerProvider.getWebSocketContainer()
                    .connectToServer(UserStatusClients.ExpiredTokenClient.class, uri)) {
                // should never reach here
            }
        })
                .isNotNull()
                .extracting(Throwable::getSuppressed)
                .asInstanceOf(InstanceOfAssertFactories.ARRAY)
                .singleElement()
                .asInstanceOf(InstanceOfAssertFactories.THROWABLE)
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isInstanceOf(WebSocketClientHandshakeException.class)
                .hasFieldOrPropertyWithValue("message", "Invalid handshake response getStatus: 401 Unauthorized");
    }

    @Test
    public void sessionCanWriteBeforeAndAfterTokenExpiration() throws Exception {
        var aliceUUID = UUID.fromString("3b8274a9-aa64-47bc-89a7-64cf896aae93");
        var bobUUID = UUID.fromString("b3c53ad9-cbd9-4644-8371-c2468d8a0e57");
        var aliceStatus = new UserStatus(aliceUUID, 6.0f, List.of(), List.of());
        var bobStatus = new UserStatus(bobUUID, 2.0f, List.of(), List.of());

        alice.getAsyncRemote()
                .sendObject(aliceStatus);
        bob.getAsyncRemote()
                .sendObject(bobStatus);

        Thread.sleep(10000); // sleep 10 seconds to ensure JWT is expired

        var newAliceStatus = new UserStatus(aliceUUID, 10.0f, List.of(new ItemStatus(0, "L", 1)), List.of());
        var newBobStatus = new UserStatus(bobUUID, 11.0f, List.of(new ItemStatus(1110, "G", 100)), List.of());

        alice.getAsyncRemote()
                .sendObject(newAliceStatus);
        bob.getAsyncRemote()
                .sendObject(newBobStatus);

        Thread.sleep(1000);

        var allAliceStatuses = UserStatusClients.ALICE_RECEIVED.stream()
                .toList();
        var allBobStatuses = UserStatusClients.BOB_RECEIVED.stream()
                .toList();

        assertThat(allAliceStatuses)
                .isNotNull()
                .contains(bobStatus, aliceStatus, newBobStatus, newAliceStatus);
        assertThat(allBobStatuses)
                .isNotNull()
                .contains(bobStatus, newBobStatus);
    }

    @AfterEach
    public void tearDown() throws Exception {
        alice.close();
        bob.close();
        UserStatusClients.ALICE_RECEIVED.clear();
        UserStatusClients.BOB_RECEIVED.clear();
    }
}
