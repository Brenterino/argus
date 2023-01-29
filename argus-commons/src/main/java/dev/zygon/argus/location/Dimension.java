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
 * Default available dimensions that a location can be tracked across. Can be
 * converted to the Minecraft equivalent through subtracting one from the
 * ordinal value.
 * <p>
 * Example conversion to Minecraft equivalent ID:
 * <p>
 * <code>
 * var dimension = Dimension.OVERWORLD; <br/>
 * var dimensionId = dimension.ordinal() - 1;
 * </code>
 * </p>
 * </p>
 */
public enum Dimension {

    NETHER,
    OVERWORLD,
    END
}
