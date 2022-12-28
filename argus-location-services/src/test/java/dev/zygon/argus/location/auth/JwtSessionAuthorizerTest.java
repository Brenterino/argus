package dev.zygon.argus.location.auth;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.permission.Permission;
import dev.zygon.argus.permission.Permissions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.websocket.Session;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static dev.zygon.argus.location.auth.JwtSessionAuthorizer.PERMISSION_ATTRIBUTE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtSessionAuthorizerTest {

    @Mock
    private JsonWebToken token;

    @InjectMocks
    private JwtSessionAuthorizer authorizer;

    @Mock
    private Session session;

    private Group estalia;
    private Group butternut;
    private Group pavia;
    private Permissions permissions;

    @BeforeEach
    void setUp() {
        estalia = new Group("Estalia");
        butternut = new Group("Butternut");
        pavia = new Group("Pavia");
        var permissionMap = Map.of(
                estalia, Permission.READWRITE,
                butternut, Permission.READ,
                pavia, Permission.ADMIN
        );
        permissions = new Permissions(permissionMap);
    }

    @Test
    void whenTokenHasNullClaimedGroupsSessionIsNotAuthorized() {
        when(token.claim(Claims.groups))
                .thenReturn(Optional.empty());

        var result = authorizer.authorize(session);

        verify(token, times(1))
                .claim(Claims.groups);
        verify(session, times(1))
                .getId();
        verifyNoMoreInteractions(token, session);

        assertThat(result)
                .isFalse();
    }

    @Test
    void whenTokenHasNoClaimedGroupsSessionIsNotAuthorized() {
        when(token.claim(Claims.groups))
                .thenReturn(Optional.of(Collections.emptySet()));

        var result = authorizer.authorize(session);

        verify(token, times(1))
                .claim(Claims.groups);
        verify(session, times(1))
                .getId();
        verifyNoMoreInteractions(token, session);

        assertThat(result)
                .isFalse();
    }

    @Test
    void whenTokenHasClaimedGroupsSessionIsAuthorized() {
        var propertyMap = new HashMap<String, Object>();

        when(token.claim(Claims.groups))
                .thenReturn(Optional.of(permissions.toRaw()));
        when(session.getUserProperties())
                .thenReturn(propertyMap);

        var result = authorizer.authorize(session);

        verify(token, times(1))
                .claim(Claims.groups);
        verify(session, times(1))
                .getUserProperties();
        verifyNoMoreInteractions(token, session);

        assertThat(result)
                .isTrue();
        assertThat(propertyMap)
                .containsKey(PERMISSION_ATTRIBUTE_NAME)
                .extractingByKey(PERMISSION_ATTRIBUTE_NAME)
                .extracting("permissions")
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsAllEntriesOf(permissions.permissions());
    }

    @Test
    void whenSessionHasNoPermissionsReadGroupsIsEmptyStream() {
        when(session.getUserProperties())
                .thenReturn(Collections.emptyMap());

        var readGroups = authorizer.readGroups(session);

        verify(session, times(1))
                .getUserProperties();
        verifyNoMoreInteractions(session);
        verifyNoInteractions(token);

        assertThat(readGroups)
                .isEmpty();
    }

    @Test
    void whenSessionHasNoPermissionsWriteGroupsIsEmptyStream() {
        when(session.getUserProperties())
                .thenReturn(Collections.emptyMap());

        var writeGroups = authorizer.writeGroups(session);

        verify(session, times(1))
                .getUserProperties();
        verifyNoMoreInteractions(session);
        verifyNoInteractions(token);

        assertThat(writeGroups)
                .isEmpty();
    }

    @Test
    void whenSessionHasPermissionsReadGroupsHasGroups() {
        var sessionMap = Map.
                <String, Object>of(PERMISSION_ATTRIBUTE_NAME, permissions);

        when(session.getUserProperties())
                .thenReturn(sessionMap);

        var readGroups = authorizer.readGroups(session);

        verify(session, times(1))
                .getUserProperties();
        verifyNoMoreInteractions(session);
        verifyNoInteractions(token);

        assertThat(readGroups)
                .isNotEmpty()
                .containsExactlyInAnyOrder(estalia, butternut, pavia);
    }

    @Test
    void whenSessionHasPermissionsWriteGroupsHasGroups() {
        var sessionMap = Map.
                <String, Object>of(PERMISSION_ATTRIBUTE_NAME, permissions);

        when(session.getUserProperties())
                .thenReturn(sessionMap);

        var writeGroups = authorizer.writeGroups(session);

        verify(session, times(1))
                .getUserProperties();
        verifyNoMoreInteractions(session);
        verifyNoInteractions(token);

        assertThat(writeGroups)
                .isNotEmpty()
                .containsExactlyInAnyOrder(estalia, pavia);
    }
}
