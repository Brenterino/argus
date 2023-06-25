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
package dev.zygon.argus.location;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.auth.SessionAuthorizer;
import dev.zygon.argus.location.messaging.GroupLocationsLocalRelay;
import dev.zygon.argus.location.session.SessionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.websocket.Session;
import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationsSocketTest {

    @Mock
    private SessionAuthorizer authorizer;

    @Mock
    private SessionRegistry<Group> registry;

    @Mock
    private GroupLocationsLocalRelay relay;

    @InjectMocks
    private LocationsSocket socket;

    @Mock
    private Session session;

    private Group butternut;

    @BeforeEach
    void setUp() {
        butternut = new Group("Butternut");
    }

    @Test
    void whenSessionIsOpenedAndSessionIsNotAuthorizedThenSessionIsClosed() throws Exception {
        when(authorizer.authorize(session))
                .thenReturn(false);

        socket.onOpen(session);

        verify(authorizer, times(1))
                .authorize(session);
        verify(session, times(1))
                .getId();
        verify(session, times(1))
                .close();
        verifyNoMoreInteractions(authorizer, session);
        verifyNoInteractions(registry, relay);
    }

    @Test
    void whenSessionIsOpenedAndSessionIsAuthorizedButSessionHasNoReadGroupsThenNoRegistrationOccurs() throws Exception {
        when(authorizer.authorize(session))
                .thenReturn(true);
        when(authorizer.readGroups(session))
                .thenReturn(Stream.empty());

        socket.onOpen(session);

        verify(authorizer, times(1))
                .authorize(session);
        verify(authorizer, times(1))
                .readGroups(session);
        verify(session, times(1))
                .getId();
        verifyNoMoreInteractions(authorizer, session);
        verifyNoInteractions(registry, relay);
    }

    @Test
    void whenSessionIsOpenedAndSessionIsAuthorizedThenSessionIsRegisteredForReadGroups() throws Exception {
        when(authorizer.authorize(session))
                .thenReturn(true);
        when(authorizer.readGroups(session))
                .thenReturn(Stream.of(butternut));

        socket.onOpen(session);

        verify(authorizer, times(1))
                .authorize(session);
        verify(authorizer, times(1))
                .readGroups(session);
        verify(registry, times(1))
                .add(butternut, session);
        verify(session, times(1))
                .getId();
        verifyNoMoreInteractions(authorizer, registry, session);
        verifyNoInteractions(relay);
    }

    @Test
    void whenSessionIsClosedAndSessionHadNoGroupsThenSessionIsNotDeregistered() {
        when(authorizer.readGroups(session))
                .thenReturn(Stream.empty());

        socket.onClose(session);

        verify(authorizer, times(1))
                .readGroups(session);
        verify(session, times(1))
                .getId();
        verifyNoMoreInteractions(authorizer, session);
        verifyNoInteractions(registry, relay);
    }

    @Test
    void whenSessionIsClosedAndSessionHasReadGroupsThenSessionIsDeregistered() {
        when(authorizer.readGroups(session))
                .thenReturn(Stream.of(butternut));

        socket.onClose(session);

        verify(authorizer, times(1))
                .readGroups(session);
        verify(registry, times(1))
                .remove(butternut, session);
        verify(session, times(1))
                .getId();
        verifyNoMoreInteractions(authorizer, registry, session);
        verifyNoInteractions(relay);
    }

    @Test
    void whenSessionHasErrorThenMessageIsLogged() {
        socket.onError(session, new Throwable("Canned exception"));

        verify(session, times(1))
                .getId();
        verifyNoMoreInteractions(session);
        verifyNoInteractions(authorizer, registry, relay);
    }

    @Nested
    class MessageTests {

        private Locations locations;

        @BeforeEach
        void setUp() {
            locations = new Locations(Collections.emptySet());
        }

        @Test
        void whenMessageIsReceivedButSessionHasNoWriteGroupsNoMessagesAreSent() {
            when(authorizer.writeGroups(session))
                    .thenReturn(Stream.empty());

            socket.onMessage(session, locations);

            verify(authorizer, times(1))
                    .writeGroups(session);
            verifyNoMoreInteractions(authorizer);
            verifyNoInteractions(registry, relay, session);
        }

        @Test
        void whenMessageIsReceivedAndSessionHasWriteGroupsMessagesAreSent() {
            when(authorizer.writeGroups(session))
                    .thenReturn(Stream.of(butternut));

            socket.onMessage(session, locations);

            verify(authorizer, times(1))
                    .writeGroups(session);
            verify(relay, times(1))
                    .receive(butternut, locations);
            verifyNoMoreInteractions(authorizer, relay);
            verifyNoInteractions(registry, session);
        }
    }
}
