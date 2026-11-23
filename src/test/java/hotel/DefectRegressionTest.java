package hotel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for the defects found in the first version (v1) of the project.
 * Each test failed on v1 and passes after the fix. They are kept in the suite so that
 * the same defects cannot come back unnoticed. Run only these with:
 *   mvn test -Dgroups=regression
 */
@Tag("regression")
@DisplayName("Regression tests for defects found in v1")
class DefectRegressionTest {

    private HotelReservationSystem system;

    @BeforeEach
    void setUp() {
        system = new HotelReservationSystem(
                HotelReservationSystemTest.clockAt(LocalDateTime.of(2026, 12, 1, 10, 0)));
        system.addRoom(new Room(101, 2, "50.05"));
    }

    @Test
    @DisplayName("DEF-01 check-in date in the past must be rejected")
    void def01PastCheckIn() {
        assertThrows(IllegalArgumentException.class, () -> system.makeReservation(
                new Guest("Alice"), 101, LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 3), 1));
    }

    @Test
    @DisplayName("DEF-02 stays longer than 30 nights must be rejected")
    void def02UnlimitedStay() {
        LocalDate in = LocalDate.of(2026, 12, 10);
        assertThrows(IllegalArgumentException.class, () -> system.makeReservation(
                new Guest("Alice"), 101, in, in.plusDays(31), 1));
    }

    @Test
    @DisplayName("DEF-03 invalid prices (NaN, Infinity, huge values) must be rejected")
    void def03InvalidPrice() {
        // v1 used double: new Room(1, 1, Double.NaN) was accepted because NaN <= 0 is false.
        // v2 uses BigDecimal, which cannot hold NaN/Infinity, and checks an upper limit.
        assertThrows(NumberFormatException.class, () -> new BigDecimal("NaN"));
        assertThrows(IllegalArgumentException.class, () -> new Room(1, 1, "1E+308"));
        assertThrows(IllegalArgumentException.class, () -> new Room(1, 1, "100000.01"));
    }

    @Test
    @DisplayName("DEF-04 50.05 x 3 nights must be exactly 150.15 (no floating-point error)")
    void def04FloatingPointCost() {
        LocalDate monday = LocalDate.of(2026, 12, 7);
        assertEquals(new BigDecimal("150.15"),
                system.calculateCost(101, monday, monday.plusDays(3), false));
    }
}
