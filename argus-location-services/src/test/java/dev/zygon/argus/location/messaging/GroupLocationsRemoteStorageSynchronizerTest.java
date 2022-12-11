package dev.zygon.argus.location.messaging;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.GroupLocations;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.location.UserLocation;
import dev.zygon.argus.location.storage.GroupLocationsStorage;
import dev.zygon.argus.user.User;
import io.vertx.core.json.JsonObject;
import org.assertj.core.api.InstanceOfAssertFactories;
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

import static org.assertj.core.api.Assertions.assertThat;
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
        var ping = new User("PING-1", "AnimeReviewer");
        var brit = new User("1", "BritishWanderer");
        var creepi0n = new User("2", "Creepi0n");
        var gobblin = new User("3", "Gobblin");
        var locationPing = new Location(500, 70, -3000, 0, true, Instant.now());
        var locationBrit = new Location(500, 65, -3000, 0, true, Instant.now());
        var locationCreepi0n = new Location(-2000, -32, 2000, 0, true, Instant.now());
        var locationGobblin = new Location(0, 0, 0, 1, false, Instant.now());
        var userLocationPing = new UserLocation(ping, locationPing);
        var userLocationBrit = new UserLocation(brit, locationBrit);
        var userLocationCreepi0n = new UserLocation(creepi0n, locationCreepi0n);
        var userLocationGobblin = new UserLocation(gobblin, locationGobblin);

        pavia = new Group("Pavia");
        locations = new Locations(Set.of(userLocationPing, userLocationBrit,
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
                .asInstanceOf(InstanceOfAssertFactories.COLLECTION)
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
