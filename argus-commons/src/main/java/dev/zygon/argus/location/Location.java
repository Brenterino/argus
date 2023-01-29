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
package dev.zygon.argus.location;

import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;

/**
 * Record which contains location data. This location could be related to an
 * entity, waypoint (permanent/temporary), or for other purposes.
 *
 * @param x     the x-coordinate position.
 * @param y     the y-coordinate position.
 * @param z     the z-coordinate position.
 * @param w     which dimension the position is in.
 * @param local if the position was determined from a local observer.
 *              <b>May only be relevant for some purposes.</b>
 * @param time  the time the location was captured.
 *              May only be relevant for some purposes.
 */
@Builder
public record Location(double x, double y, double z, int w,
                       boolean local, @NonNull Instant time) {
}
