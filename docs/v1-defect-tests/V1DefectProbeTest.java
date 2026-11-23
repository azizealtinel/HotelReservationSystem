package hotel;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/** The DEF-01..04 regression tests rewritten for the v1 API and run against v1. */
class V1DefectProbeTest {
    HotelReservationSystem s;
    @BeforeEach void setUp() { s = new HotelReservationSystem(); s.addRoom(new Room(101, 2, 50.05)); }

    @Test void DEF01_pastCheckIn() {
        assertThrows(IllegalArgumentException.class,
            () -> s.makeReservation("Alice", 101, LocalDate.of(2020,1,1), LocalDate.of(2020,1,3), 1));
    }
    @Test void DEF02_unlimitedStay() {
        LocalDate in = LocalDate.of(2026,12,10);
        assertThrows(IllegalArgumentException.class,
            () -> s.makeReservation("Alice", 101, in, in.plusDays(31), 1));
    }
    @Test void DEF03_nanPrice() {
        assertThrows(IllegalArgumentException.class, () -> new Room(1, 1, Double.NaN));
    }
    @Test void DEF03b_infinitePrice() {
        assertThrows(IllegalArgumentException.class, () -> new Room(1, 1, Double.POSITIVE_INFINITY));
    }
    @Test void DEF04_floatingPointCost() {
        LocalDate in = LocalDate.of(2026,12,7);
        assertEquals(150.15, s.calculateCost(101, in, in.plusDays(3)));
    }
}
