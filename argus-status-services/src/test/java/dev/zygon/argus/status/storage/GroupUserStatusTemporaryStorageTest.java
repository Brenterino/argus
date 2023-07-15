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
package dev.zygon.argus.status.storage;

import dev.zygon.argus.group.Group;
import dev.zygon.argus.status.EffectStatus;
import dev.zygon.argus.status.GroupUserStatuses;
import dev.zygon.argus.status.ItemStatus;
import dev.zygon.argus.status.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;

@ExtendWith(MockitoExtension.class)
class GroupUserStatusTemporaryStorageTest {

    @InjectMocks
    private GroupUserStatusTemporaryStorage storage;

    private Group memeTeam;
    private UserStatus status;

    @BeforeEach
    void setUp() {
        var uuid = UUID.randomUUID();
        var item = new ItemStatus(0, "L", 5);
        var itemTwo = new ItemStatus(0, "L", 7);
        var effect = new EffectStatus(0, "G", Instant.parse("2022-11-23T18:17:44.323877200Z"));
        var originalStatus = new UserStatus(uuid, 16.5f, List.of(item), List.of(effect));
        status = new UserStatus(uuid, 12.0f, List.of(itemTwo), List.of(effect));
        memeTeam = new Group("MemeTeam");
        storage.setLocalStorageExpirationSeconds(10);
        storage.track(memeTeam, originalStatus);
    }

    @Test
    void whenTrackingAndNewDataIsReceivedItReplacesExistingData() {
        storage.track(memeTeam, status);

        var groupStatuses = storage.collect();

        assertThat(groupStatuses)
                .extracting(GroupUserStatuses::statuses)
                .first()
                .asInstanceOf(COLLECTION)
                .first()
                .isEqualTo(status);
    }
}
