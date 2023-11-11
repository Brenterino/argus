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
import dev.zygon.argus.status.ItemStatus;
import dev.zygon.argus.status.UserStatus;
import dev.zygon.argus.session.SessionRegistry;
import dev.zygon.argus.status.storage.GroupUserStatusStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupStatusesTemporaryStorageRelayTest {

    @Mock
    private GroupUserStatusStorage storage;

    @Mock
    private SessionRegistry<Group, UserStatus> registry;

    @InjectMocks
    private GroupStatusesTemporaryStorageRelay relay;

    private Group volterra;
    private UserStatus status;

    @BeforeEach
    void setUp() {
        var uuid = UUID.randomUUID();
        var item = new ItemStatus(0, "L", 5);
        var effect = new EffectStatus(0, "G", Instant.parse("2022-11-23T18:17:44.323877200Z"));
        status = new UserStatus(uuid, 15.0f, List.of(item), List.of(effect));
        volterra = new Group("volterra");
    }

    @Test
    void whenDataIsReceivedItIsPutInStorage() {
        relay.receive(volterra, status);

        verify(storage, times(1))
                .track(volterra, status);
        verify(registry, times(1))
                .broadcast(volterra, status);
        verifyNoMoreInteractions(storage, registry);
    }

    @Test
    void whenRelayIsInvokedMessagesAreBroadcastToRegistry() {
        relay.relay(volterra, status);

        verify(registry, times(1))
                .broadcast(volterra, status);
        verifyNoMoreInteractions(registry);
        verifyNoInteractions(storage);
    }
}
