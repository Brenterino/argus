package dev.zygon.argus.location;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.location.auth.SessionAuthorizer;
import dev.zygon.argus.location.codec.LocationsDecoder;
import dev.zygon.argus.location.codec.LocationsEncoder;
import dev.zygon.argus.location.messaging.GroupLocationsLocalRelay;
import dev.zygon.argus.location.session.SessionRegistry;
import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.RequestScoped;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@Slf4j
@ServerEndpoint(value = "/locations", decoders = LocationsDecoder.class, encoders = LocationsEncoder.class)
@RequestScoped
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

    @OnClose
    public void onClose(Session session) {
        log.info("Session with ID ({}) closed", session.getId());
        authorizer.readGroups(session)
                .forEach(group -> registry.remove(group, session));
    }

    @OnError
    public void onError(Session session, Throwable cause) {
        log.error("Session with ID ({}) closed due to error",
                session.getId(), cause);
    }

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
