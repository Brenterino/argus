package dev.zygon.argus.location.storage;

import dev.zygon.argus.location.Location;

public interface LocationPriorityStrategy {

    boolean shouldReplace(Location previous, Location possibleNext);
}
