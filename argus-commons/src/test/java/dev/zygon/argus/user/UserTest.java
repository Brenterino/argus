package dev.zygon.argus.user;

import dev.zygon.argus.group.Group;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void cannotCreateUserWithoutUUIDAndName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(null, null));
        assertThrows(IllegalArgumentException.class, () ->
                new User(UUID.randomUUID(), null));
        assertThrows(IllegalArgumentException.class, () ->
                new User(null, "acuhdemiic"));
    }

    @Test
    void hashCodeIsUUIDHashcode() {
        var sauceUuid = UUID.randomUUID();
        var creepUuid = UUID.randomUUID();
        var sauce = new User(sauceUuid, "ShadySauce");
        var creep = new User(creepUuid, "Creepi0n");

        assertEquals(sauceUuid.hashCode(), sauce.hashCode());
        assertEquals(creepUuid.hashCode(), creep.hashCode());
    }

    @Nested
    class EqualsHashCodeCases {

        private final User walkers;
        private final User maybeWalkers;
        private final User sometimesWalkers;
        private final User notWalkers;
        private final Group definitelyNotWalkers;

        EqualsHashCodeCases() {
            var sharedUuid = UUID.randomUUID();
            var uniqueUuid = UUID.randomUUID();
            walkers = new User(sharedUuid, "Walkers");
            maybeWalkers = new User(sharedUuid, "Chicken");
            sometimesWalkers = new User(sharedUuid, "WalkersGaming");
            notWalkers = new User(uniqueUuid, "FriedChicken");
            definitelyNotWalkers = new Group("Walkers");
        }

        @Test
        void equalsAndHashCodeAreReflective() {
            assertEquals(walkers, walkers);
            assertEquals(walkers.hashCode(), walkers.hashCode());
        }

        @Test
        void equalsAndHashCodeAreSymmetric() {
            assertEquals(walkers, maybeWalkers);
            assertEquals(maybeWalkers, walkers);
            assertEquals(walkers.hashCode(), maybeWalkers.hashCode());
        }

        @Test
        void equalsAndHashCodeAreTransitive() {
            assertEquals(walkers, maybeWalkers); // a = b
            assertEquals(maybeWalkers, sometimesWalkers); // b = c
            assertEquals(walkers, sometimesWalkers); // then, a = c
            assertEquals(walkers.hashCode(), maybeWalkers.hashCode());
            assertEquals(maybeWalkers.hashCode(), sometimesWalkers.hashCode());
            assertEquals(walkers.hashCode(), sometimesWalkers.hashCode());
        }

        @Test
        void equalsAndHashCodeAreConsistent() {
            assertEquals(walkers, maybeWalkers);
            assertEquals(walkers, maybeWalkers);
            assertNotEquals(walkers, notWalkers);
            assertNotEquals(walkers, notWalkers);
            assertEquals(walkers.hashCode(), maybeWalkers.hashCode());
            assertEquals(walkers.hashCode(), maybeWalkers.hashCode());
            assertNotEquals(walkers.hashCode(), notWalkers.hashCode());
            assertNotEquals(walkers.hashCode(), notWalkers.hashCode());
        }

        @Test
        void equalsOnNullIsFalse() {
            assertNotEquals(null, walkers);
            assertNotEquals(null, maybeWalkers);
            assertNotEquals(null, sometimesWalkers);
        }

        @Test
        void equalsOnDifferentTypeIsFalse() {
            assertNotEquals(walkers, definitelyNotWalkers);
        }
    }
}
