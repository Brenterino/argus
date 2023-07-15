/*
    Argus - Suite of services aimed to enhance Minecraft Multiplayer
    Copyright (C) 2023 Zygon

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.zygon.argus.location.storage;

import dev.zygon.argus.location.Coordinate;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;

/**
 * Implementation of {@link LocationPriorityStrategy} which is the default
 * strategy for replacement. This implementation compares the timestamp for
 * which the location data was captured and will select the location data
 * with the most recent data based on capture time.
 */
@ApplicationScoped
public class DefaultLocationPriorityStrategy implements LocationPriorityStrategy {

    @Override
    public boolean shouldReplace(Coordinate previous, @NonNull Coordinate possibleNext) {
        if (previous == null)
            return true;

        var previousTime = previous.time();
        var possibleNextTime = possibleNext.time();
        return possibleNextTime.isAfter(previousTime);
    }
}
