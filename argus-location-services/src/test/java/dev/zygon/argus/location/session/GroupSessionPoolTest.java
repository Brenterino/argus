package dev.zygon.argus.location.session;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.Locations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupSessionPoolTest {

    @Mock
    private Session sessionOne;

    @Mock
    private Session sessionTwo;

    private GroupSessionPool pool;

    @BeforeEach
    void setUp() {
        var group = new Group("MTA");
        pool = new GroupSessionPool(group);
    }

    @Nested
    class AddRemoveSessionTests {

        @BeforeEach
        void setUp() {
            when(sessionOne.getId())
                    .thenReturn("1");
            when(sessionTwo.getId())
                    .thenReturn("2");
        }

        @Test
        void canAddSessions() {
            pool.add(sessionOne);
            pool.add(sessionTwo);

            verify(sessionOne, times(1))
                    .getId();
            verify(sessionTwo, times(1))
                    .getId();
            verifyNoMoreInteractions(sessionOne, sessionTwo);
        }

        @Test
        void cannotAddDuplicateSessions() {
            pool.add(sessionOne);
            pool.add(sessionTwo);
            pool.add(sessionOne);
            pool.add(sessionTwo);

            verify(sessionOne, times(3))
                    .getId();
            verify(sessionTwo, times(3))
                    .getId();
            verifyNoMoreInteractions(sessionOne, sessionTwo);
        }

        @Test
        void canRemoveSessions() {
            pool.add(sessionOne);
            pool.add(sessionTwo);

            pool.remove(sessionOne);
            pool.remove(sessionTwo);

            verify(sessionOne, times(2))
                    .getId();
            verify(sessionTwo, times(2))
                    .getId();
            verifyNoMoreInteractions(sessionOne, sessionTwo);
        }

        @Test
        void cannotRemoveSessionsNotRegistered() {
            pool.remove(sessionOne);
            pool.remove(sessionTwo);

            verify(sessionOne, times(2))
                    .getId();
            verify(sessionTwo, times(2))
                    .getId();
            verifyNoMoreInteractions(sessionOne, sessionTwo);
        }
    }

    @Nested
    class BroadcastTests {

        private Locations locations;

        @BeforeEach
        void setUp() {
            locations = new Locations(Collections.emptySet());
        }

        @Test
        void willBroadcastToNoSessionsIfNoneAreAdded() {
            pool.broadcast(locations);

            verifyNoInteractions(sessionOne, sessionTwo);
        }

        @Test
        void willBroadcastToOnlySessionsAdded() {
            var callbackCaptor = ArgumentCaptor.forClass(SendHandler.class);
            var async = mock(Async.class);
            var result = new SendResult();
            when(sessionOne.getId())
                    .thenReturn("1");
            when(sessionOne.getAsyncRemote())
                    .thenReturn(async);
            doNothing()
                    .when(async)
                    .sendObject(eq(locations), any());

            pool.add(sessionOne);

            pool.broadcast(locations);

            verify(sessionOne, times(1))
                    .getId();
            verify(sessionOne, times(1))
                    .getAsyncRemote();
            verify(async, times(1))
                    .sendObject(eq(locations), callbackCaptor.capture());
            verifyNoMoreInteractions(sessionOne, async);
            verifyNoInteractions(sessionTwo);

            var callback = callbackCaptor.getValue();
            callback.onResult(result);
        }

        @Test
        void willBroadcastToAllSessionsAdded() {
            var callbackCaptor = ArgumentCaptor.forClass(SendHandler.class);
            var async = mock(Async.class);
            var result = new SendResult();
            var resultTwo = new SendResult(new Throwable("Canned exception"));
            when(sessionOne.getId())
                    .thenReturn("1");
            when(sessionTwo.getId())
                    .thenReturn("2");
            when(sessionOne.getAsyncRemote())
                    .thenReturn(async);
            when(sessionTwo.getAsyncRemote())
                    .thenReturn(async);

            pool.add(sessionOne);
            pool.add(sessionTwo);

            pool.broadcast(locations);

            verify(sessionOne, times(1))
                    .getId();
            verify(sessionOne, times(1))
                    .getAsyncRemote();
            verify(sessionTwo, times(1))
                    .getId();
            verify(sessionTwo, times(1))
                    .getAsyncRemote();
            verify(async, times(2))
                    .sendObject(eq(locations), callbackCaptor.capture());
            verifyNoMoreInteractions(sessionOne, sessionTwo, async);

            var callbacks = callbackCaptor.getAllValues();

            callbacks.forEach(callback -> callback.onResult(result));
            callbacks.forEach(callback -> callback.onResult(resultTwo));
        }
    }

    @Nested
    class ActiveTests {

        @Test
        void poolNotActiveWithNoSessions() {
            var result = pool.active();

            verifyNoInteractions(sessionOne, sessionTwo);

            assertThat(result)
                    .isFalse();
        }

        @Test
        void poolActiveWithSessions() {
            when(sessionOne.getId())
                    .thenReturn("1");
            when(sessionTwo.getId())
                    .thenReturn("2");

            pool.add(sessionOne);
            pool.add(sessionTwo);

            var result = pool.active();

            verify(sessionOne, times(1))
                    .getId();
            verify(sessionTwo, times(1))
                    .getId();
            verifyNoMoreInteractions(sessionOne, sessionTwo);

            assertThat(result)
                    .isTrue();
        }
    }
}
