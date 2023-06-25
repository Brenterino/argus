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
package dev.zygon.argus.location.session;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.Locations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.websocket.Session;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupSessionRegistryTest {

    @Mock
    private Session session;

    @Mock
    private GroupSessionPool pool;

    private Group group;
    private GroupSessionRegistry registry;

    @BeforeEach
    void setUp() {
        group = new Group("Weebs");
        registry = new GroupSessionRegistry();
        registry.getPools()
                .put(group, pool);
    }

    @Test
    void canAddSessionsToPool() {
        registry.add(group, session);

        verify(pool, times(1))
                .add(session);
        verifyNoMoreInteractions(pool);
        verifyNoInteractions(session);
    }

    @Test
    void canRemoveSessionFromPool() {
        registry.remove(group, session);

        verify(pool, times(1))
                .remove(session);
        verifyNoMoreInteractions(pool);
        verifyNoInteractions(session);
    }

    @Nested
    class BroadcastTest {

        private Locations locations;

        @BeforeEach
        void setUp() {
            locations = new Locations(Collections.emptySet());
        }

        @Test
        void willNotBroadcastToPoolIfInactive() {
            when(pool.active())
                    .thenReturn(false);

            registry.broadcast(group, locations);

            verify(pool, times(1))
                    .active();
            verifyNoMoreInteractions(pool);
            verifyNoInteractions(session);
        }

        @Test
        void willBroadcastToPoolIfActive() {
            when(pool.active())
                    .thenReturn(true);

            registry.broadcast(group, locations);

            verify(pool, times(1))
                    .active();
            verify(pool, times(1))
                    .broadcast(locations);
            verifyNoMoreInteractions(pool);
            verifyNoInteractions(session);
        }
    }
}
