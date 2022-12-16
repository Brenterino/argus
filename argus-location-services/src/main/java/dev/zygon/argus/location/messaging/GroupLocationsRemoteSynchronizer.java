package dev.zygon.argus.location.messaging;

import io.smallrye.mutiny.Multi;
import io.vertx.core.json.JsonObject;

/**
 * Abstraction for synchronizer which can send and receive location data
 * to/from remote instances for the purposes of synchronizing this data
 * across all instances for distribution to clients.
 */
public interface GroupLocationsRemoteSynchronizer {

    /**
     * Method which is invoked whenever a message is received from a remote
     * instance. Will come in the form of a raw JSON message and must be
     * converted into the appropriate message.
     *
     * @param rawMessage the raw JSON message which was received from a remote
     *                   instance sending data.
     */
    void receive(JsonObject rawMessage);

    /**
     * Creates a {@link Multi} which is used to publish data from this instance
     * to remote instances for the purposes of synchronization.
     *
     * @return a {@link Multi} which is used to publish location data out to
     * other instances.
     */
    Multi<GroupLocationsMessage> send();
}
