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

import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

/**
 * Wrapper record which contains a set of user location data that can be
 * received from or sent to a user.
 * <p>
 * The contents are contained within a set to avoid duplication of user data.
 * This is possible because the implementation of hashCode and equals only take
 * the specified user into account.
 * </p>
 *
 * @param data set of user location data contained in this record.
 */
public record Locations(@NonNull Set<UserLocation> data) {

    /**
     * Constructor for Locations which the data of user locations.
     *
     * @param data the user location data.
     */
    public Locations(Set<UserLocation> data) {
        this.data = data != null ? Collections.unmodifiableSet(data) :
                Collections.emptySet();
    }
}
