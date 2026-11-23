package hotel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class HotelReservationSystemTest {
    private HotelReservationSystem system;
    private final LocalDate checkIn = LocalDate.of(2026, 12, 10);
    private final LocalDate checkOut = LocalDate.of(2026, 12, 12);

    @BeforeEach
    void setUp() {
        system = new HotelReservationSystem();
        system.addRoom(new Room(101, 2, 100.0));
        system.addRoom(new Room(102, 4, 150.0));
    }

    // Equivalence Partitioning: valid guest counts 1..capacity and invalid partitions.
    @Test
    void shouldAcceptMinimumGuestCount() {
        Reservation reservation = system.makeReservation("Alice", 101, checkIn, checkOut, 1);
        assertEquals(1, reservation.getGuestCount());
    }

    @Test
    void shouldAcceptMaximumGuestCount() {
        Reservation reservation = system.makeReservation("Alice", 101, checkIn, checkOut, 2);
        assertEquals(2, reservation.getGuestCount());
    }

    @Test
    void shouldRejectGuestCountAboveCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> system.makeReservation("Alice", 101, checkIn, checkOut, 3));
    }

    @Test
    void shouldRejectZeroGuestCount() {
        assertThrows(IllegalArgumentException.class,
                () -> system.makeReservation("Alice", 101, checkIn, checkOut, 0));
    }

    // Boundary Value Analysis: checkout equal to and one day after check-in.
    @Test
    void shouldRejectSameCheckInAndCheckOutDate() {
        LocalDate sameDay = LocalDate.of(2026, 12, 10);
        assertThrows(IllegalArgumentException.class,
                () -> system.makeReservation("Alice", 101, sameDay, sameDay, 1));
    }

    @Test
    void shouldAcceptOneNightStay() {
        LocalDate oneNightOut = checkIn.plusDays(1);
        Reservation reservation = system.makeReservation("Alice", 101, checkIn, oneNightOut, 1);
        assertEquals(checkIn, reservation.getCheckIn());
        assertEquals(oneNightOut, reservation.getCheckOut());
    }

    // Decision table: existing room + valid dates + capacity + availability.
    @Test
    void shouldRejectUnknownRoom() {
        assertThrows(IllegalArgumentException.class,
                () -> system.makeReservation("Alice", 999, checkIn, checkOut, 1));
    }

    @Test
    void shouldRejectBlankGuestName() {
        assertThrows(IllegalArgumentException.class,
                () -> system.makeReservation("   ", 101, checkIn, checkOut, 1));
    }

    @Test
    void shouldRejectOverlappingReservation() {
        system.makeReservation("Alice", 101, checkIn, checkOut, 1);

        assertThrows(IllegalStateException.class,
                () -> system.makeReservation("Bob", 101,
                        checkIn.plusDays(1), checkOut.plusDays(1), 1));
    }

    @Test
    void shouldAllowReservationStartingOnPreviousCheckoutDate() {
        system.makeReservation("Alice", 101, checkIn, checkOut, 1);

        Reservation second = system.makeReservation("Bob", 101,
                checkOut, checkOut.plusDays(2), 1);

        assertEquals("Bob", second.getGuestName());
    }

    // Negative testing.
    @Test
    void shouldRejectDuplicateRoomNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> system.addRoom(new Room(101, 3, 120.0)));
    }

    @Test
    void shouldRejectNullRoom() {
        assertThrows(IllegalArgumentException.class, () -> system.addRoom(null));
    }

    // Cost calculation.
    @Test
    void shouldCalculateTotalCostForTwoNights() {
        double cost = system.calculateCost(101, checkIn, checkOut);
        assertEquals(200.0, cost);
    }

    @Test
    void shouldRejectInvalidCostDates() {
        assertThrows(IllegalArgumentException.class,
                () -> system.calculateCost(101, checkOut, checkIn));
    }

    // Regression: a successful reservation still increments the count.
    @Test
    void shouldIncreaseReservationCountAfterSuccessfulReservation() {
        assertEquals(0, system.getReservationCount());
        system.makeReservation("Alice", 101, checkIn, checkOut, 1);
        assertEquals(1, system.getReservationCount());
    }
}
