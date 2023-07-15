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
package dev.zygon.argus.location;

import dev.zygon.argus.user.User;
import io.quarkus.test.common.http.TestHTTPResource;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static dev.zygon.argus.location.client.LocationsClients.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Disabled("Only meant to be used as a base test and should not run directly.")
public class LocationsSocketIT {

    @TestHTTPResource("/locations")
    private URI uri;

    private Session alice;
    private Session bob;

    @BeforeEach
    public void setUp() throws Exception {
        alice = ContainerProvider.getWebSocketContainer()
                .connectToServer(AliceClient.class, uri);
        bob = ContainerProvider.getWebSocketContainer()
                .connectToServer(BobClient.class, uri);
        ALICE_RECEIVED.clear();
        BOB_RECEIVED.clear();
    }

    @Test
    public void testLocationMessage() throws Exception {
        var aliceUUID = UUID.fromString("3b8274a9-aa64-47bc-89a7-64cf896aae93");
        var bobUUID = UUID.fromString("b3c53ad9-cbd9-4644-8371-c2468d8a0e57");
        var userAlice = new User(aliceUUID, "Alice");
        var userBob = new User(bobUUID, "Bob");
        var locationAlice = new Coordinate(101, 50, 2100, 0, true, Instant.now());
        var locationBob = new Coordinate(-100, 0, -100, 0, true, Instant.now());
        var userLocationAlice = new Location(userAlice, LocationType.USER, locationAlice);
        var userLocationBob = new Location(userBob, LocationType.USER, locationBob);
        var aliceLocations = new Locations(Set.of(userLocationAlice));
        var bobLocations = new Locations(Set.of(userLocationBob));

        alice.getAsyncRemote()
                .sendObject(aliceLocations);
        bob.getAsyncRemote()
                .sendObject(bobLocations);

        Thread.sleep(1000);

        var allAliceLocations = ALICE_RECEIVED.stream()
                .toList();
        var allBobLocations = BOB_RECEIVED.stream()
                .toList();

        assertThat(allAliceLocations)
                .isNotNull();
        assertThat(allAliceLocations)
                .flatExtracting(Locations::data)
                .isNotNull()
                .extracting(Location::user)
                .contains(userBob, userAlice);
        assertThat(allAliceLocations)
                .flatExtracting(Locations::data)
                .isNotNull()
                .extracting(Location::coordinates)
                .contains(locationBob, locationAlice);
        assertThat(allBobLocations)
                .isNotNull();
        assertThat(allBobLocations)
                .flatExtracting(Locations::data)
                .isNotNull()
                .extracting(Location::user)
                .contains(userBob);
        assertThat(allBobLocations)
                .flatExtracting(Locations::data)
                .isNotNull()
                .extracting(Location::coordinates)
                .contains(locationBob);
    }

    @AfterEach
    public void tearDown() throws Exception {
        alice.close();
        bob.close();
        ALICE_RECEIVED.clear();
        BOB_RECEIVED.clear();
    }
}
