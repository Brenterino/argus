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
package dev.zygon.argus.permission;

import lombok.NonNull;

import java.util.Objects;
import java.util.UUID;

public record UserPermission(@NonNull UUID uuid, @NonNull Permission permission) {

    /**
     * Custom implementation of the equals function. It is recommended to
     * review the documentation of {@link Object#equals(Object)} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, equality can be determined by the
     * equality of the UUID alone.
     * </p>
     *
     * @param o the reference object with which to compare against this
     *          object.
     * @return if this object equals the provided object per the standard
     * contract for {@link Object#equals(Object)}.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof UserPermission userPermission &&
                Objects.equals(uuid, userPermission.uuid);
    }

    /**
     * Custom implementation of the hashCode function. It is recommended to
     * review the documentation of {@link Object#hashCode()} for more
     * information on the criteria which this implementation must satisfy.
     * <p>
     * As it pertains to this implementation, hashCode can be computed by the
     * UUID alone.
     * </p>
     *
     * @return the hashCode of this record based on the standard contract for
     * {@link Object#hashCode()}.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
