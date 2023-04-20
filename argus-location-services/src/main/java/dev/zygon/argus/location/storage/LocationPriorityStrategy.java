package dev.zygon.argus.location.storage;

import dev.zygon.argus.location.Coordinate;

/**
 * Abstraction for an algorithm to determine if the currently held location
 * data should be evicted from storage and replaced with the next available
 * data.
 */
public interface LocationPriorityStrategy {

    /**
     * Determine if the current location data should be replaced by the next
     * location.
     *
     * @param previous     the current location which will be evicted if the
     *                     next location should replace it based on the
     *                     implementation.
     * @param possibleNext the location which could replace the currently held
     *                     location based on the implementation.
     * @return if the current location data should be replaced by the next
     * location.
     */
    boolean shouldReplace(Coordinate previous, Coordinate possibleNext);
}
