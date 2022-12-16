package dev.zygon.argus.location.auth;

import dev.zygon.argus.group.Group;

import javax.websocket.Session;
import java.util.stream.Stream;

/**
 * Provides an abstraction for determining authorization of a {@link Session}
 * and determining which read/write groups the session is authorized to access
 * for the respective permission.
 */
public interface SessionAuthorizer {

    /**
     * Determines if the session is authorized for general access to the
     * location services or not.<br/>
     * <b>Important:</b> Implementations of this function may not be pure
     * and could have side effects such as mutating the {@link Session} or
     * storage held by the session.
     *
     * @param session the session which will be verified for access.
     * @return if the session is authorized for access to anything within
     * location services.
     */
    boolean authorize(Session session);

    /**
     * Retrieves the groups for which the session is authorized for read
     * access in the form of a stream.
     *
     * @param session the session for which read access to groups will be
     *                evaluated.
     * @return a stream of groups the session has read access to.
     */
    Stream<Group> readGroups(Session session);

    /**
     * Retrieves the groups for which the session is authorized for write
     * access in the form of a stream.
     *
     * @param session the session for which write access to groups will be
     *                evaluated.
     * @return a stream of groups the session has write access to.
     */
    Stream<Group> writeGroups(Session session);
}
