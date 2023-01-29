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

/**
 * Enumeration of available permissions to a group that may be assigned to
 * members of a group.
 */
public enum Permission {

    /**
     * Permissions which denotes the user has the ability to access resources
     * related to the group but has not elected their access.
     * <p>
     * This is the default assigned elected permission for a user once they
     * are newly added to a group.
     * </p>
     */
    ACCESS,
    /**
     * Permission which grants the ability to read information from/about the
     * group.
     */
    READ,
    /**
     * Permission which grants the ability to write information to/about the
     * group.
     */
    WRITE,
    /**
     * Permission which grants the ability to read and write information
     * to/about the group.
     */
    READWRITE,
    /**
     * Permission which grants the ability to control access levels of other
     * group members.
     */
    ADMIN;

    /**
     * @return if this permission level grants read access to a group.
     */
    public boolean canRead() {
        return this == READWRITE ||
                this == READ ||
                this == ADMIN;
    }

    /**
     * @return if this permission level grants write access to a group.
     */
    public boolean canWrite() {
        return this == READWRITE ||
                this == WRITE ||
                this == ADMIN;
    }
}
