package dev.zygon.argus.client.location;

import dev.zygon.argus.location.Location;

import java.util.UUID;

public enum LocalLocationStorage {

    INSTANCE;

    public void track(UUID target, Location location) {
        // enqueue location data
    }
}
