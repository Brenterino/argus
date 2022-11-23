package dev.zygon.argus.user;

import dev.zygon.argus.group.Group;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void cannotCreateUserWithoutUUIDAndName() {
        assertThrows(NullPointerException.class, () ->
                new User(null, null));
        assertThrows(NullPointerException.class, () ->
                new User("1", null));
        assertThrows(NullPointerException.class, () ->
                new User(null, "acuhdemiic"));
    }

    @Test
    void metadataCannotBeMutated() {
        var sauce = new User("1", "ShadySauce", Map.of("note", "POS"));

        assertThrows(UnsupportedOperationException.class, () ->
                sauce.metadata().remove("note"));
    }

    @Test
    void hashCodeIsUUIDHashcode() {
        var sauce = new User("1", "ShadySauce", Map.of("note", "POS"));
        var creep = new User("2", "Creepi0n", Map.of("note", "Explosive"));

        assertEquals("1".hashCode(), sauce.hashCode());
        assertEquals("2".hashCode(), creep.hashCode());
    }

    @Nested
    class EqualsHashCodeCases {

        private final User walkers;
        private final User maybeWalkers;
        private final User sometimesWalkers;
        private final User notWalkers;
        private final Group definitelyNotWalkers;

        EqualsHashCodeCases() {
            walkers = new User("3", "Walkers", Map.of("alias", "WalkersGaming",
                    "note", "gaming"));
            maybeWalkers = new User("3", "Chicken", Map.of("alias", "Flappy",
                    "note", "Not Gaming"));
            sometimesWalkers = new User("3", "WalkersGaming", Map.of("alias", "",
                    "note", "GAMING"));
            notWalkers = new User("4", "FriedChicken", Map.of("alias", "Tasty",
                    "note", "Mango Habanero"));
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
