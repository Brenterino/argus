package dev.zygon.argus.location.session;

import dev.zygon.argus.location.Locations;

import javax.websocket.Session;

public interface SessionRegistry<K> {

    void add(K key, Session session);

    void remove(K key, Session session);

    void broadcast(K key, Locations locations);
}
