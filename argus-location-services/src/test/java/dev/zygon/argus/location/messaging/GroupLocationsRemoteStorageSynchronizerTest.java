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
package dev.zygon.argus.location.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.GroupLocations;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.UserLocation;
import dev.zygon.argus.location.storage.GroupLocationsStorage;
import dev.zygon.argus.user.User;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupLocationsRemoteStorageSynchronizerTest {

    @Mock
    private GroupLocationsStorage storage;

    @InjectMocks
    private GroupLocationsRemoteStorageSynchronizer synchronizer;

    private Group pavia;
    private Locations locations;
    private GroupLocationsMessage message;

    @BeforeEach
    void setUp() {
        var brit = new User(UUID.randomUUID(), "BritishWanderer");
        var creepi0n = new User(UUID.randomUUID(), "Creepi0n");
        var gobblin = new User(UUID.randomUUID(), "Gobblin");
        var locationBrit = new Location(500, 65, -3000, 0, true, Instant.now());
        var locationCreepi0n = new Location(-2000, -32, 2000, 0, true, Instant.now());
        var locationGobblin = new Location(0, 0, 0, 1, false, Instant.now());
        var userLocationBrit = new UserLocation(brit, locationBrit);
        var userLocationCreepi0n = new UserLocation(creepi0n, locationCreepi0n);
        var userLocationGobblin = new UserLocation(gobblin, locationGobblin);

        pavia = new Group("Pavia");
        locations = new Locations(Set.of(userLocationBrit,
                userLocationCreepi0n, userLocationGobblin));
        var groupLocations = new GroupLocations(pavia, locations);
        message = new GroupLocationsMessage(Set.of(groupLocations));
        synchronizer.setRemoteRelayPublishDelayMillis(1);
    }

    @Test
    void whenReceivingGroupLocationsMessageWithNoDataNothingIsStored() {
        var emptyMessage = new GroupLocationsMessage(Collections.emptySet());
        var json = JsonObject.mapFrom(emptyMessage);

        synchronizer.receive(json);

        verifyNoInteractions(storage);
    }

    @Test
    void whenReceivingGroupLocationsMessageWithDataStorageIsUpdated() {
        var json = JsonObject.mapFrom(message);
        var captor = ArgumentCaptor.forClass(Locations.class);

        synchronizer.receive(json);

        verify(storage, times(1))
                .track(eq(pavia), captor.capture());
        verifyNoMoreInteractions(storage);

        assertThat(captor.getValue())
                .extracting(Locations::data)
                .asInstanceOf(COLLECTION)
                .containsExactlyInAnyOrderElementsOf(locations.data());
    }

    @Test
    void whenMultiRelayIsInvokedMessagesAreRelayed() {
        when(storage.collect())
                .thenReturn(Set.of(new GroupLocations(pavia, locations)));

        var sentMessage = synchronizer.send()
                .toUni()
                .await()
                .atMost(Duration.ofMillis(50));

        var data = sentMessage.data();

        verify(storage, times(1))
                .collect();
        verifyNoMoreInteractions(storage);

        assertThat(data)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);
        assertThat(data)
                .first()
                .extracting(GroupLocations::group)
                .isEqualTo(pavia);
        assertThat(data)
                .first()
                .extracting(GroupLocations::locations)
                .isEqualTo(locations);
    }
}
