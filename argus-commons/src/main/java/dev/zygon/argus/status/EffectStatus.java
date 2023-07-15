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
package dev.zygon.argus.status;

import lombok.NonNull;

import java.time.Instant;

public record EffectStatus(int color, @NonNull String symbol, @NonNull Instant expiration)
        implements Comparable<EffectStatus> {

    @Override
    public int compareTo(@NonNull EffectStatus o) {
        var symbolCompare = symbol.compareTo(o.symbol);
        return symbolCompare != 0 ? symbolCompare : Integer.compare(color, o.color);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiration);
    }
}
