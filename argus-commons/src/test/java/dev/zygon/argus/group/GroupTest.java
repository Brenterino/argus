package dev.zygon.argus.group;

import dev.zygon.argus.user.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    @Test
    void cannotCreateGroupWithoutName() {
        assertThrows(IllegalArgumentException.class, () ->
                new Group(null));
    }

    @Test
    void metadataCannotBeMutated() {
        var estalia = new Group("Estalia", Map.of("events", "Donut Days on Wednesday"));

        assertThrows(UnsupportedOperationException.class, () ->
                estalia.metadata().remove("events"));
    }

    @Test
    void hashCodeIsNamespaceAndNameHashcode() {
        var argus = new Group("Argus");
        var hash = Objects.hash(argus.namespace(), argus.name());

        assertEquals(hash, argus.hashCode());
    }

    @Test
    void toStringIsNamespaceAndName() {
        var butternut = new Group("Butternut");
        var blue = new Group("Helios", "Blue");

        assertEquals("DEFAULT-Butternut", butternut.toString());
        assertEquals("Helios-Blue", blue.toString());
    }

    @Nested
    class EqualsHashCodeCases {

        private final Group kallos;
        private final Group maybeKallos;
        private final Group sometimesKallos;
        private final Group notKallos;
        private final User definitelyNotKallos;

        EqualsHashCodeCases() {
            kallos = new Group("Kallos", Map.of("kallos", "kallos"));
            maybeKallos = new Group("Kallos", Map.of("sussy", "baka"));
            sometimesKallos = new Group("Kallos", Map.of("raiding?", "aaa, you know what it is"));
            notKallos = new Group("Butternut");
            definitelyNotKallos = new User(UUID.randomUUID(), "xxTBxx");
        }

        @Test
        void equalsAndHashCodeAreReflective() {
            assertEquals(kallos, kallos);
            assertEquals(kallos.hashCode(), kallos.hashCode());
        }

        @Test
        void equalsAndHashCodeAreSymmetric() {
            assertEquals(kallos, maybeKallos);
            assertEquals(maybeKallos, kallos);
            assertEquals(kallos.hashCode(), maybeKallos.hashCode());
        }

        @Test
        void equalsAndHashCodeAreTransitive() {
            assertEquals(kallos, maybeKallos); // a = b
            assertEquals(maybeKallos, sometimesKallos); // b = c
            assertEquals(kallos, sometimesKallos); // then, a = c
            assertEquals(kallos.hashCode(), maybeKallos.hashCode());
            assertEquals(maybeKallos.hashCode(), sometimesKallos.hashCode());
            assertEquals(kallos.hashCode(), sometimesKallos.hashCode());
        }

        @Test
        void equalsAndHashCodeAreConsistent() {
            assertEquals(kallos, maybeKallos);
            assertEquals(kallos, maybeKallos);
            assertNotEquals(kallos, notKallos);
            assertNotEquals(kallos, notKallos);
            assertEquals(kallos.hashCode(), maybeKallos.hashCode());
            assertEquals(kallos.hashCode(), maybeKallos.hashCode());
            assertNotEquals(kallos.hashCode(), notKallos.hashCode());
            assertNotEquals(kallos.hashCode(), notKallos.hashCode());
        }

        @Test
        void equalsOnNullIsFalse() {
            assertNotEquals(null, kallos);
            assertNotEquals(null, maybeKallos);
            assertNotEquals(null, sometimesKallos);
        }

        @Test
        void equalsOnDifferentTypeIsFalse() {
            assertNotEquals(kallos, definitelyNotKallos);
        }
    }
}
