package hotel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HotelReservationSystem")
class HotelReservationSystemTest {

    static final ZoneId ZONE = ZoneId.of("Europe/Istanbul");
    // "Today" in all tests is Tuesday 2026-12-01, 10:00
    static final LocalDate TODAY = LocalDate.of(2026, 12, 1);

    private HotelReservationSystem system;
    private final Guest alice = new Guest("Alice");
    private final LocalDate checkIn = LocalDate.of(2026, 12, 10);
    private final LocalDate checkOut = LocalDate.of(2026, 12, 15);

    static Clock clockAt(LocalDateTime time) {
        return Clock.fixed(time.atZone(ZONE).toInstant(), ZONE);
    }

    @BeforeEach
    void setUp() {
        system = new HotelReservationSystem(clockAt(TODAY.atTime(10, 0)));
        system.addRoom(new Room(101, 2, "100.00"));
        system.addRoom(new Room(102, 4, "150.00"));
    }

    @Nested
    @DisplayName("Adding rooms (B9) - negative tests")
    class AddRoom {

        @Test
        @DisplayName("TC-H01 duplicate room number is rejected")
        void duplicateRoom() {
            assertThrows(IllegalArgumentException.class, () -> system.addRoom(new Room(101, 3, "120.00")));
            assertEquals(2, system.getRooms().size());
        }

        @Test
        @DisplayName("TC-H02 null room is rejected")
        void nullRoom() {
            assertThrows(IllegalArgumentException.class, () -> system.addRoom(null));
        }

        @Test
        @DisplayName("TC-H03 returned room list cannot be modified from outside")
        void roomListIsReadOnly() {
            assertThrows(UnsupportedOperationException.class,
                    () -> system.getRooms().add(new Room(999, 1, "10.00")));
        }

        @Test
        @DisplayName("TC-H04 null clock is rejected")
        void nullClock() {
            assertThrows(IllegalArgumentException.class, () -> new HotelReservationSystem(null));
        }
    }

    @Nested
    @DisplayName("Dates (B2-B5) - BVA")
    class Dates {

        @Test
        @DisplayName("TC-H05 check-in today is accepted, yesterday is rejected (B3)")
        void checkInTodayBoundary() {
            assertDoesNotThrow(() -> system.makeReservation(alice, 101, TODAY, TODAY.plusDays(1), 1));
            assertThrows(IllegalArgumentException.class,
                    () -> system.makeReservation(alice, 102, TODAY.minusDays(1), TODAY.plusDays(1), 1));
        }

        @Test
        @DisplayName("TC-H06 check-in 365 days ahead accepted, 366 rejected (B4)")
        void advanceBookingBoundary() {
            LocalDate last = TODAY.plusDays(365);
            assertDoesNotThrow(() -> system.makeReservation(alice, 101, last, last.plusDays(1), 1));
            LocalDate tooFar = TODAY.plusDays(366);
            assertThrows(IllegalArgumentException.class,
                    () -> system.makeReservation(alice, 102, tooFar, tooFar.plusDays(1), 1));
        }

        @ParameterizedTest(name = "TC-H07 {0} night(s) accepted")
        @ValueSource(ints = {1, 2, 29, 30})
        void validStayLengths(int nights) {
            Reservation r = system.makeReservation(alice, 101, checkIn, checkIn.plusDays(nights), 1);
            assertEquals(nights, r.getNights());
        }

        @ParameterizedTest(name = "TC-H08 {0} night(s) rejected")
        @ValueSource(ints = {-1, 0, 31, 60})
        void invalidStayLengths(int nights) {
            assertThrows(IllegalArgumentException.class,
                    () -> system.makeReservation(alice, 101, checkIn, checkIn.plusDays(nights), 1));
        }

        @Test
        @DisplayName("TC-H09 null dates are rejected")
        void nullDates() {
            assertThrows(IllegalArgumentException.class,
                    () -> system.makeReservation(alice, 101, null, checkOut, 1));
            assertThrows(IllegalArgumentException.class,
                    () -> system.makeReservation(alice, 101, checkIn, null, 1));
        }
    }

    @Nested
    @DisplayName("Guest count (B6) - EP + BVA")
    class GuestCount {

        @ParameterizedTest(name = "TC-H10 room 102 (capacity 4), {0} guest(s) accepted")
        @ValueSource(ints = {1, 2, 3, 4})
        void validGuestCounts(int count) {
            assertEquals(count, system.makeReservation(alice, 102, checkIn, checkOut, count).getGuestCount());
        }

        @ParameterizedTest(name = "TC-H11 room 102 (capacity 4), {0} guest(s) rejected")
        @ValueSource(ints = {-1, 0, 5})
        void invalidGuestCounts(int count) {
            assertThrows(IllegalArgumentException.class,
                    () -> system.makeReservation(alice, 102, checkIn, checkOut, count));
        }
    }

    @Nested
    @DisplayName("Overlapping reservations (B7, B8)")
    class Overlap {

        @BeforeEach
        void existingReservation() {
            // room 101 is booked 2026-12-10 .. 2026-12-15
            system.makeReservation(alice, 101, checkIn, checkOut, 1);
        }

        @ParameterizedTest(name = "TC-H12 {2}: {0} -> {1} rejected")
        @CsvSource({
                "2026-12-10, 2026-12-15, same dates",
                "2026-12-08, 2026-12-11, overlaps the start",
                "2026-12-14, 2026-12-17, overlaps the end",
                "2026-12-11, 2026-12-13, inside existing stay",
                "2026-12-08, 2026-12-17, contains existing stay",
                "2026-12-14, 2026-12-15, last night only"
        })
        void overlappingStaysRejected(LocalDate in, LocalDate out, String description) {
            Guest bob = new Guest("Bob");
            assertThrows(IllegalStateException.class, () -> system.makeReservation(bob, 101, in, out, 1));
            assertEquals(1, system.getActiveReservationCount());
        }

        @ParameterizedTest(name = "TC-H13 {2}: {0} -> {1} accepted")
        @CsvSource({
                "2026-12-15, 2026-12-17, starts on previous check-out day",
                "2026-12-08, 2026-12-10, ends on existing check-in day",
                "2026-12-03, 2026-12-06, completely before",
                "2026-12-20, 2026-12-22, completely after"
        })
        void nonOverlappingStaysAccepted(LocalDate in, LocalDate out, String description) {
            assertDoesNotThrow(() -> system.makeReservation(new Guest("Bob"), 101, in, out, 1));
        }

        @Test
        @DisplayName("TC-H14 same dates in a different room are accepted")
        void differentRoom() {
            assertDoesNotThrow(() -> system.makeReservation(new Guest("Bob"), 102, checkIn, checkOut, 1));
        }

        @Test
        @DisplayName("TC-H15 cancelled reservation frees the room (B8)")
        void cancelledReservationFreesRoom() {
            assertFalse(system.isRoomAvailable(101, checkIn, checkOut));
            system.cancelReservation("R0001");
            assertTrue(system.isRoomAvailable(101, checkIn, checkOut));
            assertDoesNotThrow(() -> system.makeReservation(new Guest("Bob"), 101, checkIn, checkOut, 1));
        }
    }

    @Nested
    @DisplayName("makeReservation decision table")
    class ReservationDecisionTable {

        /*
         * Condition            | 1 | 2 | 3 | 4 | 5 | 6
         * Guest given          | Y | N | Y | Y | Y | Y
         * Room exists          | Y | - | N | Y | Y | Y
         * Dates valid          | Y | - | - | N | Y | Y
         * Guest count valid    | Y | - | - | - | N | Y
         * Room available       | Y | - | - | - | - | N
         * -------------------- |---|---|---|---|---|---
         * Result               |OK |IAE|IAE|IAE|IAE|ISE
         * (IAE = IllegalArgumentException, ISE = IllegalStateException, - = not evaluated)
         */
        @Test
        @DisplayName("TC-H16 rule 1: all conditions true -> reservation created")
        void allValid() {
            Reservation r = system.makeReservation(alice, 101, checkIn, checkOut, 2);
            assertAll(
                    () -> assertEquals("R0001", r.getId()),
                    () -> assertEquals("Alice", r.getGuest().getName()),
                    () -> assertEquals(101, r.getRoomNumber()),
                    () -> assertEquals(Reservation.Status.ACTIVE, r.getStatus()),
                    () -> assertEquals(1, system.getActiveReservationCount()));
        }

        @Test
        @DisplayName("TC-H17 rule 2: no guest -> rejected")
        void noGuest() {
            assertThrows(IllegalArgumentException.class,
                    () -> system.makeReservation(null, 101, checkIn, checkOut, 1));
        }

        @Test
        @DisplayName("TC-H18 rule 3: unknown room -> rejected")
        void unknownRoom() {
            assertThrows(IllegalArgumentException.class,
                    () -> system.makeReservation(alice, 999, checkIn, checkOut, 1));
        }

        @Test
        @DisplayName("TC-H19 rule 4: invalid dates -> rejected")
        void invalidDates() {
            assertThrows(IllegalArgumentException.class,
                    () -> system.makeReservation(alice, 101, checkOut, checkIn, 1));
        }

        @Test
        @DisplayName("TC-H20 rule 5: too many guests -> rejected")
        void tooManyGuests() {
            assertThrows(IllegalArgumentException.class,
                    () -> system.makeReservation(alice, 101, checkIn, checkOut, 3));
        }

        @Test
        @DisplayName("TC-H21 rule 6: room not available -> rejected with IllegalStateException")
        void notAvailable() {
            system.makeReservation(alice, 101, checkIn, checkOut, 1);
            assertThrows(IllegalStateException.class,
                    () -> system.makeReservation(new Guest("Bob"), 101, checkIn, checkOut, 1));
        }

        @Test
        @DisplayName("TC-H22 nothing is stored when a reservation is rejected")
        void rejectedReservationIsNotStored() {
            assertThrows(IllegalArgumentException.class,
                    () -> system.makeReservation(alice, 101, checkIn, checkOut, 3));
            assertEquals(0, system.getActiveReservationCount());
            assertTrue(system.isRoomAvailable(101, checkIn, checkOut));
        }
    }

    @Nested
    @DisplayName("Price and IDs")
    class PriceAndIds {

        @Test
        @DisplayName("TC-H23 total price is stored on the reservation (loyalty member)")
        void totalPriceStored() {
            // 2026-12-10 (Thu) .. 2026-12-15 (Tue): 100+120+120+100+100 = 540, minus 10% loyalty = 486.00
            Reservation r = system.makeReservation(new Guest("Ayşe", true), 101, checkIn, checkOut, 1);
            assertEquals(new BigDecimal("486.00"), r.getTotalPrice());
            assertEquals(r.getTotalPrice(), system.calculateCost(101, checkIn, checkOut, true));
        }

        @Test
        @DisplayName("TC-H24 IDs are sequential: R0001, R0002")
        void sequentialIds() {
            assertEquals("R0001", system.makeReservation(alice, 101, checkIn, checkOut, 1).getId());
            assertEquals("R0002", system.makeReservation(alice, 102, checkIn, checkOut, 1).getId());
        }

        @Test
        @DisplayName("TC-H25 price quote for unknown room is rejected")
        void costForUnknownRoom() {
            assertThrows(IllegalArgumentException.class,
                    () -> system.calculateCost(999, checkIn, checkOut, false));
        }
    }

    @Nested
    @DisplayName("Cancellation")
    class Cancellation {

        @Test
        @DisplayName("TC-H26 cancelling 9 days before check-in refunds 100%")
        void fullRefund() {
            Reservation r = system.makeReservation(alice, 101, checkIn, checkOut, 1);
            assertEquals(new BigDecimal("540.00"), system.cancelReservation(r.getId()));
            assertEquals(Reservation.Status.CANCELLED, r.getStatus());
            assertEquals(new BigDecimal("540.00"), r.getRefundAmount());
            assertEquals(0, system.getActiveReservationCount());
        }

        @Test
        @DisplayName("TC-H27 cancelling 36 hours before check-in refunds 50%")
        void halfRefund() {
            // now = 2026-12-09 02:00, check-in = 2026-12-10 14:00
            HotelReservationSystem late = new HotelReservationSystem(
                    clockAt(LocalDateTime.of(2026, 12, 9, 2, 0)));
            late.addRoom(new Room(101, 2, "100.00"));
            Reservation r = late.makeReservation(alice, 101, checkIn, checkOut, 1);
            assertEquals(new BigDecimal("270.00"), late.cancelReservation(r.getId()));
        }

        @Test
        @DisplayName("TC-H28 cancelling twice is rejected")
        void cancelTwice() {
            Reservation r = system.makeReservation(alice, 101, checkIn, checkOut, 1);
            system.cancelReservation(r.getId());
            assertThrows(IllegalStateException.class, () -> system.cancelReservation(r.getId()));
        }

        @Test
        @DisplayName("TC-H29 cancelling an unknown reservation is rejected")
        void cancelUnknown() {
            assertThrows(IllegalArgumentException.class, () -> system.cancelReservation("R9999"));
            assertThrows(IllegalArgumentException.class, () -> system.cancelReservation(null));
        }

        @Test
        @DisplayName("TC-H30 cancelling on the check-in day after 14:00 is rejected")
        void cancelAfterCheckIn() {
            HotelReservationSystem s = new HotelReservationSystem(clockAt(TODAY.atTime(15, 0)));
            s.addRoom(new Room(101, 2, "100.00"));
            Reservation r = s.makeReservation(alice, 101, TODAY, TODAY.plusDays(2), 1);
            assertThrows(IllegalStateException.class, () -> s.cancelReservation(r.getId()));
            assertTrue(r.isActive());
        }
    }

    @Nested
    @DisplayName("isRoomAvailable - negative tests")
    class Availability {

        @Test
        @DisplayName("TC-H31 unknown room or invalid dates are reported as not available")
        void invalidInputs() {
            assertFalse(system.isRoomAvailable(999, checkIn, checkOut));
            assertFalse(system.isRoomAvailable(101, null, checkOut));
            assertFalse(system.isRoomAvailable(101, checkIn, null));
            assertFalse(system.isRoomAvailable(101, checkOut, checkIn));
            assertTrue(system.isRoomAvailable(101, checkIn, checkOut));
        }
    }
}
