package dev.zygon.argus.location.auth;

import dev.zygon.argus.group.Group;

import javax.websocket.Session;
import java.util.stream.Stream;

public interface SessionAuthorizer {

    boolean authorize(Session session);

    Stream<Group> readGroups(Session session);

    Stream<Group> writeGroups(Session session);
}
