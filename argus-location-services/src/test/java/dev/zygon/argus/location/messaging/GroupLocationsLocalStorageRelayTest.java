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
import dev.zygon.argus.location.Coordinate;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.LocationType;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.session.SessionRegistry;
import dev.zygon.argus.location.storage.GroupLocationsStorage;
import dev.zygon.argus.user.User;
import jakarta.websocket.RemoteEndpoint.Async;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupLocationsLocalStorageRelayTest {

    @Mock
    private GroupLocationsStorage storage;

    @Mock
    private SessionRegistry<Group> registry;

    @InjectMocks
    private GroupLocationsLocalStorageRelay relay;

    private Group volterra;
    private Locations locations;

    @BeforeEach
    void setUp() {
        var mickale = new User(UUID.randomUUID(), "Mickale");
        var hoover = new User(UUID.randomUUID(), "ItzHoover");
        var s4nta = new User(UUID.randomUUID(), "S4NTA");
        var locationMickale = new Coordinate(-1066, 0, -1200, 0, true, Instant.now());
        var locationHoover = new Coordinate(-1000, 25, 200, 0, true, Instant.now());
        var locationS4nta = new Coordinate(0, -50, 0, 1, true, Instant.now());
        var userLocationMickale = new Location(mickale, LocationType.USER, locationMickale);
        var userLocationHoover = new Location(hoover, LocationType.USER, locationHoover);
        var userLocationS4nta = new Location(s4nta, LocationType.USER, locationS4nta);

        volterra = new Group("volterra");
        locations = new Locations(Set.of(userLocationMickale, userLocationHoover, userLocationS4nta));
    }

    @Test
    void whenDataIsReceivedItIsPutInStorage() {
        relay.receive(volterra, locations);

        verify(storage, times(1))
                .track(volterra, locations);
        verify(registry, times(1))
                .broadcast(volterra, locations);
        verifyNoMoreInteractions(storage, registry);
    }

    @Test
    void whenRelayIsInvokedMessagesAreBroadcastToRegistry() {
        relay.relay(volterra, locations);

        verify(registry, times(1))
                .broadcast(volterra, locations);
        verifyNoMoreInteractions(registry);
        verifyNoInteractions(storage);
    }

    @Test
    void whenReplayIsInvokedMessagesAreSentToSession() {
        var session = mock(Session.class);
        var async = mock(Async.class);

        when(session.getAsyncRemote())
                .thenReturn(async);
        when(storage.locations(volterra))
                .thenReturn(locations);

        relay.replay(volterra, session);

        verify(storage, times(1))
                .locations(volterra);
        verify(session, times(1))
                .getAsyncRemote();
        verify(async, times(1))
                .sendObject(locations);
        verifyNoMoreInteractions(storage, session, async);
    }
}
