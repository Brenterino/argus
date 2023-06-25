package dev.zygon.argus.location.storage;

import dev.zygon.argus.location.Coordinate;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of {@link LocationPriorityStrategy} which is the default
 * strategy for replacement. This implementation compares the timestamp for
 * which the location data was captured and will select the location data
 * with the most recent data based on capture time.
 */
@ApplicationScoped
public class DefaultLocationPriorityStrategy implements LocationPriorityStrategy {

    @Override
    public boolean shouldReplace(Coordinate previous, Coordinate possibleNext) {
        var previousTime = previous.time();
        var possibleNextTime = possibleNext.time();

        return possibleNextTime.isAfter(previousTime);
    }
}
