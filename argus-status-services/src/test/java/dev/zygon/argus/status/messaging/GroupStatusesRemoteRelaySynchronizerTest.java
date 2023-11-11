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

import dev.zygon.argus.group.Group;
import dev.zygon.argus.status.EffectStatus;
import dev.zygon.argus.status.GroupUserStatuses;
import dev.zygon.argus.status.ItemStatus;
import dev.zygon.argus.status.UserStatus;
import dev.zygon.argus.session.SessionRegistry;
import dev.zygon.argus.status.storage.GroupUserStatusStorage;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupStatusesRemoteRelaySynchronizerTest {

    @Mock
    private GroupUserStatusStorage storage;

    @Mock
    private SessionRegistry<Group, UserStatus> registry;

    @InjectMocks
    private GroupStatusesRemoteRelaySynchronizer synchronizer;

    private Group pavia;
    private UserStatus status;
    private Set<UserStatus> statuses;
    private UserStatusesMessage message;

    @BeforeEach
    void setUp() {
        var uuid = UUID.randomUUID();
        var item = new ItemStatus(0, "L", 5);
        var effect = new EffectStatus(0, "G", Instant.parse("2022-11-23T18:17:44.323877200Z"));
        status = new UserStatus(uuid, 20.0f, List.of(item), List.of(effect));
        statuses = Set.of(status);
        pavia = new Group("Pavia");
        var groupStatuses = new GroupUserStatuses(pavia, statuses);
        message = new UserStatusesMessage(Set.of(groupStatuses));
        synchronizer.setRemoteRelayPublishDelayMillis(1);
    }

    @Test
    void whenReceivingGroupLocationsMessageWithNoDataNothingIsStored() {
        var emptyMessage = new UserStatusesMessage(Collections.emptySet());
        var json = JsonObject.mapFrom(emptyMessage);

        synchronizer.receive(json);

        verifyNoInteractions(storage);
    }

    @Test
    void whenReceivingGroupLocationsMessageWithDataStorageIsNotUpdatedButRelayed() {
        var json = JsonObject.mapFrom(message);
        var captor = ArgumentCaptor.forClass(UserStatus.class);

        synchronizer.receive(json);

        verify(registry, times(1))
                .broadcast(eq(pavia), captor.capture());
        verifyNoMoreInteractions(registry);
        verifyNoInteractions(storage);

        assertThat(captor.getValue())
                .isEqualTo(status);
    }

    @Test
    void whenMultiRelayIsInvokedMessagesAreRelayed() {
        when(storage.collect())
                .thenReturn(Set.of(new GroupUserStatuses(pavia, statuses)));

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
                .extracting(GroupUserStatuses::group)
                .isEqualTo(pavia);
        assertThat(data)
                .first()
                .extracting(GroupUserStatuses::statuses)
                .isEqualTo(statuses);
    }
}
