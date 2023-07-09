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
package dev.zygon.argus.client.groups;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupCategory {

    private String name;
    private String color;
    private String symbol;

    public static final GroupCategory DEFAULT_CATEGORY =
            new GroupCategory("", "", "");

    public Color realColor() {
        try {
            return Color.decode(color);
        } catch (Exception e) {
            log.warn("[ARGUS] Parsing color for a group category failed. Default to WHITE. Category: {}", this);
            return Color.WHITE; // :)
        }
    }
}
