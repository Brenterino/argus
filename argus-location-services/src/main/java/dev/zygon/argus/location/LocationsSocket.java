package dev.zygon.argus.location;

import dev.zygon.argus.location.codec.LocationsDecoder;
import dev.zygon.argus.location.codec.LocationsEncoder;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@Slf4j
@ServerEndpoint(value = "/locations", decoders = LocationsDecoder.class, encoders = LocationsEncoder.class)
@ApplicationScoped
public class LocationsSocket {

    @OnOpen
    public void onOpen(Session session) {
        log.info("Session with ID ({}) opened", session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        log.info("Session with ID ({}) closed", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable cause) {
        log.error("Session with ID ({}) closed due to error",
                session.getId(), cause);
    }

    @OnMessage
    public void onMessage(Session session, Locations locations) {
        log.info("Session with ID ({}) sent data ({})",
                session.getId(), locations);
        session.getAsyncRemote()
                .sendObject(locations);
    }
}
