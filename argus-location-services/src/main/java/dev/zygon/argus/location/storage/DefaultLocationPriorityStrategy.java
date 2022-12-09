package dev.zygon.argus.location.storage;

import dev.zygon.argus.location.Location;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultLocationPriorityStrategy implements LocationPriorityStrategy {

    @Override
    public boolean shouldReplace(Location previous, Location possibleNext) {
        var previousTime = previous.time();
        var possibleNextTime = possibleNext.time();

        return possibleNextTime.isAfter(previousTime);
    }
}
