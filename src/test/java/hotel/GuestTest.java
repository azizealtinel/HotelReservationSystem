package hotel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Guest")
class GuestTest {

    @ParameterizedTest(name = "TC-G01 valid name \"{0}\"")
    @ValueSource(strings = {"Ali", "Ayşe Yılmaz", "Çağrı Öztürk", "Jean-Luc", "O'Brien", "Al"})
    void acceptsValidNames(String name) {
        assertEquals(name, new Guest(name).getName());
    }

    @ParameterizedTest(name = "TC-G02 blank name \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void rejectsNullOrBlankNames(String name) {
        assertThrows(IllegalArgumentException.class, () -> new Guest(name));
    }

    @ParameterizedTest(name = "TC-G03 invalid characters \"{0}\"")
    @ValueSource(strings = {"12345", "Ali3", "Ali@Veli", "Ayşe_Yılmaz", "Bob!"})
    void rejectsInvalidCharacters(String name) {
        assertThrows(IllegalArgumentException.class, () -> new Guest(name));
    }

    @Test
    @DisplayName("TC-G04 BVA name length: 1 rejected, 2 accepted")
    void nameLengthLowerBoundary() {
        assertThrows(IllegalArgumentException.class, () -> new Guest("A"));
        assertDoesNotThrow(() -> new Guest("Al"));
    }

    @Test
    @DisplayName("TC-G05 BVA name length: 50 accepted, 51 rejected")
    void nameLengthUpperBoundary() {
        assertDoesNotThrow(() -> new Guest("a".repeat(50)));
        assertThrows(IllegalArgumentException.class, () -> new Guest("a".repeat(51)));
    }

    @Test
    @DisplayName("TC-G06 surrounding spaces are trimmed before validation")
    void trimsName() {
        assertEquals("Ali", new Guest("  Ali  ").getName());
        assertThrows(IllegalArgumentException.class, () -> new Guest("  A  "));
    }

    @Test
    @DisplayName("TC-G07 loyalty flag is stored; default is not a member")
    void loyaltyFlag() {
        assertTrue(new Guest("Ali", true).isLoyaltyMember());
        assertFalse(new Guest("Ali").isLoyaltyMember());
    }
}
