package dev.zygon.argus.location.session;

import dev.zygon.argus.location.Locations;

import javax.websocket.Session;

public interface SessionPool {

    void add(Session session);

    void remove(Session session);

    void broadcast(Locations locations);

    boolean active();
}
