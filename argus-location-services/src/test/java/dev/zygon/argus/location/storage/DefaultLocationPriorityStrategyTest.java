package dev.zygon.argus.location.storage;

import dev.zygon.argus.location.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultLocationPriorityStrategyTest {

    private Location after;
    private Location before;
    private DefaultLocationPriorityStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DefaultLocationPriorityStrategy();

        var time = Instant.now();
        after = Location.builder()
                .time(time.plusMillis(1000))
                .build();
        before = Location.builder()
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
