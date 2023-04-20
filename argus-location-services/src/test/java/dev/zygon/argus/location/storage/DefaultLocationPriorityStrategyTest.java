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
package dev.zygon.argus.location.storage;

import dev.zygon.argus.location.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultLocationPriorityStrategyTest {

    private Coordinate after;
    private Coordinate before;
    private DefaultLocationPriorityStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DefaultLocationPriorityStrategy();

        var time = Instant.now();
        after = Coordinate.builder()
                .time(time.plusMillis(1000))
                .build();
        before = Coordinate.builder()
                .time(time)
                .build();
    }

    @Test
    void shouldReplaceIfPreviousTimeIsBeforePossibleNext() {
        var result = strategy.shouldReplace(before, after);

        assertThat(result)
                .isTrue();
    }

    @Test
    void shouldNotReplaceIfPreviousTimeIsAfterPossibleNext() {
        var result = strategy.shouldReplace(after, before);

        assertThat(result)
                .isFalse();
    }
}
