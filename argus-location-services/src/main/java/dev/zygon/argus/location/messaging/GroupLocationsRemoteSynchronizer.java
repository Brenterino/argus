package dev.zygon.argus.location.messaging;

import io.smallrye.mutiny.Multi;
import io.vertx.core.json.JsonObject;

public interface GroupLocationsRemoteSynchronizer {

    void receive(JsonObject rawMessage);

    Multi<GroupLocationsMessage> send();
}
