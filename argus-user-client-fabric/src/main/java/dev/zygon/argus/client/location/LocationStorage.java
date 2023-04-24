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
package dev.zygon.argus.client.location;

import dev.zygon.argus.client.ArgusClient;
import dev.zygon.argus.location.Coordinate;
import dev.zygon.argus.location.Location;
import dev.zygon.argus.location.LocationType;
import dev.zygon.argus.location.Locations;
import dev.zygon.argus.user.User;

import java.util.Collections;
import java.util.UUID;

public enum LocationStorage {

    INSTANCE;

    public void trackPlayer(UUID target, Coordinate coordinate) {
        var user = new User(target, "");
        var location = new Location(user, LocationType.USER, coordinate);
        // enqueue location data
    }

    public void trackPlayerMisc(UUID target, Coordinate coordinate) {
        var user = new User(target, "");
        var location = new Location(user, LocationType.MISC_USER, coordinate);
        // enqueue location data
    }

    public void syncRemote(ArgusClient client) {
        // TODO actually transmit real location data
        var locations = new Locations(Collections.emptySet());
        client.getLocations().sendLocations(locations);
    }
}
