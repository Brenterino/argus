package dev.zygon.argus.location.session;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.Locations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.websocket.Session;
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
