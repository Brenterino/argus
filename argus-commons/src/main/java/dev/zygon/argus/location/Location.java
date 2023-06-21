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

import dev.zygon.argus.user.User;
import lombok.NonNull;

import java.util.Objects;

/**
 * Wrapper record which relates a user, coordinates and a location type together.
 * <p>
 * The implementation of hashCode and equals only take the user and type
 * into account.
 * </p>
 *
 * @param user        the user which is located at a specific location or which
 *                    issued the location data.
 * @param type        the type of location this is.
 * @param coordinates the location record which contains position data for the
 *                    user.
 */
public record Location(@NonNull User user, @NonNull LocationType type, @NonNull Coordinate coordinates) {

    /**
     * Utility method to simplify creation of {@link LocationKey} record.
     *
     * @return location key which can be used to simplify logic for creation
     *         of the record.
     */
    public LocationKey key() {
        return new LocationKey(user, type);
    }

    /**
     * Custom implementation of the equals function. It is recommended to
     * review the documentation of {@link Object#equals(Object)} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, equality can be determined by the
     * equality of the user alone.
     * </p>
     *
     * @param o the reference object with which to compare against this
     *          object.
     * @return if this object equals the provided object per the standard
     * contract for {@link Object#equals(Object)}.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Location location &&
                Objects.equals(user, location.user) &&
                Objects.equals(type, location.type);
    }

    /**
     * Custom implementation of the hashCode function. It is recommended to
     * review the documentation of {@link Object#hashCode()} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, hashCode can be computed by the
     * user and the type.
     * </p>
     *
     * @return the hashCode of this record based on the standard contract for
     * {@link Object#hashCode()}.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, type);
    }
}
