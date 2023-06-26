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
package dev.zygon.argus.location.storage;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.*;
import dev.zygon.argus.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalGroupLocationsStorageTest {

    @Mock
    private LocationPriorityStrategy strategy;

    @InjectMocks
    private LocalGroupLocationsStorage storage;

    private Group memeTeam;
    private Locations originalLocations;
    private Locations locations;

    @BeforeEach
    void setUp() {
        var thoths = new User(UUID.randomUUID(), "Thoths_Librarian");
        var zygon = new User(UUID.randomUUID(), "JZygon");
        var soccer = new User(UUID.randomUUID(), "Soccer37222");
        var ladez = new User(UUID.randomUUID(), "Ladezkik");
        var originalLocationThoths = new Coordinate(99, 9, 999, 0, true, Instant.now());
        var originalLocationZygon = new Coordinate(2000, 25, 1000, 0, true, Instant.now());
        var originalLocationSoccer = new Coordinate(0, -50, 0, 0, true, Instant.now());
        var originalLocationLadez = new Coordinate(2000, 30, 2000, 0, true, Instant.now());
        var locationThoths = new Coordinate(9999, 9999, 9999, 0, true, Instant.now());
        var locationZygon = new Coordinate(-1000, 25, 200, 1, true, Instant.now());
        var locationSoccer = new Coordinate(0, -50, 0, 1, true, Instant.now());
        var locationLadez = new Coordinate(5000, 30, 1000, 0, true, Instant.now());
        var userLocationThoths = new Location(thoths, LocationType.USER, locationThoths);
        var userLocationZygon = new Location(zygon, LocationType.USER, locationZygon);
        var userLocationSoccer = new Location(soccer, LocationType.USER, locationSoccer);
        var userLocationLadez = new Location(ladez, LocationType.USER, locationLadez);
        var originalUserLocationThoths = new Location(thoths, LocationType.USER, originalLocationThoths);
        var originalUserLocationZygon = new Location(zygon, LocationType.USER, originalLocationZygon);
        var originalUserLocationSoccer = new Location(soccer, LocationType.USER, originalLocationSoccer);
        var originalUserLocationLadez = new Location(ladez, LocationType.USER, originalLocationLadez);

        memeTeam = new Group("MemeTeam");
        locations = new Locations(Set.of(userLocationThoths, userLocationZygon,
                userLocationSoccer, userLocationLadez));
        originalLocations = new Locations(Set.of(originalUserLocationThoths, originalUserLocationZygon,
                originalUserLocationSoccer, originalUserLocationLadez));

        when(strategy.shouldReplace(any(), any()))
                .thenReturn(true);
        storage.setLocalStorageExpirationMinutes(10);
        storage.track(memeTeam, originalLocations);
    }

    @Test
    void whenTrackingAndDataCannotReplaceThenNoNewDataIsAdded() {
        when(strategy.shouldReplace(any(), any()))
                .thenReturn(false);

        storage.track(memeTeam, locations);

        var groupLocations = storage.collect();

        assertThat(groupLocations)
                .extracting(GroupLocations::locations)
                .first()
                .extracting(Locations::data)
                .asInstanceOf(COLLECTION)
                .containsExactlyInAnyOrderElementsOf(originalLocations.data());
    }

    @Test
    void whenTrackingAndDataCanBeReplacedThenNewDataIsAdded() {
        when(strategy.shouldReplace(any(), any()))
                .thenReturn(true);

        storage.track(memeTeam, locations);

        var groupLocations = storage.collect();

        assertThat(groupLocations)
                .extracting(GroupLocations::locations)
                .first()
                .extracting(Locations::data)
                .asInstanceOf(COLLECTION)
                .containsExactlyInAnyOrderElementsOf(locations.data());
    }

    @Test
    void canRetrieveStoredLocationsByGroup() {
        var fakeGroup = new Group("A", "B");

        assertThat(storage.locations(memeTeam))
                .isEqualTo(originalLocations);
        assertThat(storage.locations(fakeGroup))
                .extracting(Locations::data)
                .asInstanceOf(COLLECTION)
                .isEmpty();
    }
}
