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

/**
 * Types of locations that are available to be used to distinguish certain
 * location data from one another. This allows one user to send multiple
 * different types of location types.
 */
public enum LocationType {

    /**
     * This location is the where the user is currently/last know to be at.
     */
    USER,
    /**
     * This location is related to the user/trace of the user but is not the
     * user. Example: Prison Pearl
     */
    MISC_USER,
    /**
     * This location is issued by a user to indicate to other users that it
     * may be of interest. Tends to be fairly temporary in nature.
     */
    BASIC_PING,
    /**
     * This location is issued by a user to indicate a target for focus. The
     * user associated with the location is the focus target.
     */
    FOCUS_PING
}
