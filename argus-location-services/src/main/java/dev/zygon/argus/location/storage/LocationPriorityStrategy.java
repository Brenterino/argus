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
import lombok.NonNull;

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
    boolean shouldReplace(Coordinate previous, @NonNull Coordinate possibleNext);
}
