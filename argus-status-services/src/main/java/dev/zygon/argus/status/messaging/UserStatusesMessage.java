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
package dev.zygon.argus.status.messaging;

import dev.zygon.argus.status.GroupUserStatuses;

import java.util.Set;

/**
 * Wrapper record which contains a set of {@link GroupUserStatuses} records which
 * is sent between instances via the {@link GroupStatusesRemoteSynchronizer}.
 * This record is only meant to be used as a wrapper because it is not possible
 * to directly serialize a {@link Set} into JSON.
 *
 * @param data set of all group statuses. Uniqueness is guaranteed by the
 *             implementation of {@link GroupUserStatuses#hashCode()}
 */
public record UserStatusesMessage(Set<GroupUserStatuses> data) {
}
