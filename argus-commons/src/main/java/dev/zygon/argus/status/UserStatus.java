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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record UserStatus(@NonNull UUID source, float health,
                         @NonNull List<ItemStatus> items, @NonNull List<EffectStatus> effects,
                         UserMetadata metadata) {

    public UserStatus(@NonNull UUID source, float health,
                      @NonNull List<ItemStatus> items, @NonNull List<EffectStatus> effects) {
        this(source, health, items, effects, null);
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserStatus status &&
                Objects.equals(source, status.source);
    }
}
