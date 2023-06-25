package dev.zygon.argus.location;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.auth.SessionAuthorizer;
import dev.zygon.argus.location.codec.LocationsDecoder;
import dev.zygon.argus.location.codec.LocationsEncoder;
import dev.zygon.argus.location.messaging.GroupLocationsLocalRelay;
import dev.zygon.argus.location.session.SessionRegistry;
import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Provides the endpoint for location service.
 *
 * @see ServerEndpoint
 */
@Slf4j
@ApplicationScoped
@ServerEndpoint(value = "/locations", decoders = LocationsDecoder.class, encoders = LocationsEncoder.class)
public class LocationsSocket {

    private final SessionAuthorizer authorizer;
    private final SessionRegistry<Group> registry;
    private final GroupLocationsLocalRelay relay;

    public LocationsSocket(SessionAuthorizer authorizer,
                           SessionRegistry<Group> registry,
                           GroupLocationsLocalRelay relay) {
        this.authorizer = authorizer;
        this.registry = registry;
        this.relay = relay;
    }

    /**
     * Called when a session is open per the specification of {@link OnOpen}.
     * Must be {@link Authenticated} in order for the session to be opened
     * completely. If the session is authenticated, but not authorized for
     * access, then the session will be closed.  If the session is authorized
     * for read access, then it will be added to the registry.
     *
     * @param session the session which was opened.
     * @throws Exception if something goes wrong during the logic for
     *                   session opening, then an exception is thrown.
     */
    @OnOpen
    @Authenticated
    public void onOpen(Session session) throws Exception {
        log.info("Session with ID ({}) opened", session.getId());
        if (authorizer.authorize(session)) {
            authorizer.readGroups(session)
                    .forEach(group -> registry.add(group, session));
        } else {
            session.close();
        }
    }

    /**
     * Called when the session is closed per the specification of
     * {@link OnClose}. If the session was authorized for any read access, then
     * it will be removed from the registry.
     *
     * @param session the session which was closed.
     */
    @OnClose
    public void onClose(Session session) {
        log.info("Session with ID ({}) closed", session.getId());
        authorizer.readGroups(session)
                .forEach(group -> registry.remove(group, session));
    }

    /**
     * Called when an error occurs while handling the session per the
     * specification of {@link OnError}.
     *
     * @param session the session which the error occurred with.
     * @param cause   the error.
     */
    @OnError
    public void onError(Session session, Throwable cause) {
        log.error("Session with ID ({}) closed due to error",
                session.getId(), cause);
    }

    /**
     * Called when the session receives a message per the specification of
     * {@link OnMessage}. Must be {@link Authenticated} in order for the
     * message to be received. If the session was authorized for any write
     * access, then the location data will be relayed to the groups.
     *
     * @param session   the session which the message was received from.
     * @param locations the locations which were received from the session.
     */
    @OnMessage
    @Authenticated
    public void onMessage(Session session, Locations locations) {
        if (log.isDebugEnabled()) {
            log.debug("Session with ID ({}) sent data ({})",
                    session.getId(), locations);
        }
        authorizer.writeGroups(session)
                .forEach(group -> relay.receive(group, locations));
    }
}
