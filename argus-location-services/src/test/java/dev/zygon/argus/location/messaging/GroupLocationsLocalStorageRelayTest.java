package dev.zygon.argus.location.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.GroupLocations;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.UserLocation;
import dev.zygon.argus.location.session.SessionRegistry;
import dev.zygon.argus.location.storage.GroupLocationsStorage;
import dev.zygon.argus.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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
        var mickale = new User("1", "Mickale", Map.of("Note", "Jorving Jarl"));
        var hoover = new User("2", "ItzHoover", Map.of("Note", "Jorving All Day Every Day"));
        var s4nta = new User("3", "S4NTA", Map.of("Note", "Hates Christmas"));
        var locationMickale = new Location(-1066, 0, -1200, 0, true, Instant.now());
        var locationHoover = new Location(-1000, 25, 200, 0, true, Instant.now());
        var locationS4nta = new Location(0, -50, 0, 1, true, Instant.now());
        var userLocationMickale = new UserLocation(mickale, locationMickale);
        var userLocationHoover = new UserLocation(hoover, locationHoover);
        var userLocationS4nta = new UserLocation(s4nta, locationS4nta);

        volterra = new Group("volterra");
        locations = new Locations(Set.of(userLocationMickale, userLocationHoover, userLocationS4nta));

        relay.setLocalRelayPublishDelayMillis(1);
    }

    @Test
    void whenDataIsReceivedItIsPutInStorage() {
        relay.receive(volterra, locations);

        verify(storage, times(1))
                .track(volterra, locations);
        verifyNoMoreInteractions(storage);
        verifyNoInteractions(registry);
    }

    @Test
    void whenMultiRelayIsInvokedMessagesAreRelayed() {
        when(storage.collect())
                .thenReturn(Set.of(new GroupLocations(volterra, locations)));

        var data = relay.relay()
                .toUni()
                .await()
                .atMost(Duration.ofMillis(50));

        verify(storage, times(1))
                .collect();
        verifyNoMoreInteractions(storage);
        verifyNoInteractions(registry);

        assertThat(data)
                .extracting(GroupLocations::group)
                .isEqualTo(volterra);
        assertThat(data)
                .extracting(GroupLocations::locations)
                .isEqualTo(locations);
    }

    @Test
    void whenRelayIsInvokedMessagesAreBroadcastToRegistry() {
        var groupLocations = new GroupLocations(volterra, locations);

        relay.relay(groupLocations);

        verify(registry, times(1))
                .broadcast(volterra, locations);
        verifyNoMoreInteractions(registry);
        verifyNoInteractions(storage);
    }
}
