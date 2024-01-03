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

import dev.zygon.argus.client.groups.GroupAlignmentKey;
import lombok.NonNull;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public record LocationRender(double x, double y, double z, float scale,
                             @NonNull Map<GroupAlignmentKey, AtomicInteger> alignmentDigest,
                             @NonNull List<LocationRenderEntry> entries) {

    public Color averageColor() {
        return entries.stream()
                .map(LocationRenderEntry::color)
                .reduce(Color.BLACK, (c1, c2) -> new Color(
                        (c1.getRed() + c2.getRed()) / 2,
                        (c1.getGreen() + c2.getGreen()) / 2,
                        (c1.getBlue() + c2.getBlue()) / 2));
    }
}
