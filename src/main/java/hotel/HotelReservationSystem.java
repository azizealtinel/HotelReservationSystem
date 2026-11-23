package hotel;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages rooms and reservations.
 *
 * Reservation rules:
 *  B1  the room must exist
 *  B2  check-out must be after check-in
 *  B3  check-in cannot be in the past
 *  B4  check-in can be at most 365 days in advance
 *  B5  a stay must be between 1 and 30 nights
 *  B6  guest count must be between 1 and the room capacity
 *  B7  a room cannot have overlapping active reservations
 *      (a new stay may start on the day another stay ends)
 *  B8  cancelled reservations do not block the room
 *  B9  room numbers must be unique
 *
 * A {@link Clock} is injected so that "today" can be fixed in tests.
 */
public class HotelReservationSystem {
    public static final int MAX_NIGHTS = 30;
    public static final int MAX_DAYS_IN_ADVANCE = 365;

    private final List<Room> rooms = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final PriceCalculator priceCalculator = new PriceCalculator();
    private final CancellationPolicy cancellationPolicy = new CancellationPolicy();
    private final Clock clock;
    private int nextId = 1;

    public HotelReservationSystem() {
        this(Clock.systemDefaultZone());
    }

    public HotelReservationSystem(Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("Clock cannot be null.");
        }
        this.clock = clock;
    }

    public void addRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        if (findRoom(room.getRoomNumber()).isPresent()) {
            throw new IllegalArgumentException("Room number already exists.");
        }
        rooms.add(room);
    }

    public Reservation makeReservation(Guest guest, int roomNumber, LocalDate checkIn,
                                       LocalDate checkOut, int guestCount) {
        if (guest == null) {
            throw new IllegalArgumentException("Guest cannot be null.");
        }
        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException("Room does not exist."));
        validateDates(checkIn, checkOut);
        if (guestCount < 1 || guestCount > room.getCapacity()) {
            throw new IllegalArgumentException("Guest count must be between 1 and the room capacity.");
        }
        if (!isRoomAvailable(roomNumber, checkIn, checkOut)) {
            throw new IllegalStateException("Room is not available for the selected dates.");
        }

        BigDecimal total = priceCalculator.calculateTotal(
                room.getPricePerNight(), checkIn, checkOut, guest.isLoyaltyMember());
        String id = String.format("R%04d", nextId++);
        Reservation reservation = new Reservation(
                id, guest, roomNumber, checkIn, checkOut, guestCount, total);
        reservations.add(reservation);
        return reservation;
    }

    /** Cancels a reservation and returns the refunded amount. */
    public BigDecimal cancelReservation(String reservationId) {
        Reservation reservation = findReservation(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (!reservation.isActive()) {
            throw new IllegalStateException("Reservation is already cancelled.");
        }
        BigDecimal refund = cancellationPolicy.refundAmount(
                reservation.getTotalPrice(), LocalDateTime.now(clock), reservation.getCheckIn());
        reservation.cancel(refund);
        return refund;
    }

    public boolean isRoomAvailable(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        if (findRoom(roomNumber).isEmpty()) {
            return false;
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return false;
        }
        for (Reservation r : reservations) {
            if (r.isActive() && r.getRoomNumber() == roomNumber
                    && datesOverlap(checkIn, checkOut, r.getCheckIn(), r.getCheckOut())) {
                return false;
            }
        }
        return true;
    }

    /** Price quote for a stay without making a reservation. */
    public BigDecimal calculateCost(int roomNumber, LocalDate checkIn, LocalDate checkOut,
                                    boolean loyaltyMember) {
        Room room = findRoom(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException("Room does not exist."));
        return priceCalculator.calculateTotal(room.getPricePerNight(), checkIn, checkOut, loyaltyMember);
    }

    public Optional<Reservation> findReservation(String reservationId) {
        for (Reservation r : reservations) {
            if (r.getId().equals(reservationId)) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    public int getActiveReservationCount() {
        int count = 0;
        for (Reservation r : reservations) {
            if (r.isActive()) {
                count++;
            }
        }
        return count;
    }

    public List<Room> getRooms() {
        return List.copyOf(rooms);
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Dates cannot be null.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        LocalDate today = LocalDate.now(clock);
        if (checkIn.isBefore(today)) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
        if (checkIn.isAfter(today.plusDays(MAX_DAYS_IN_ADVANCE))) {
            throw new IllegalArgumentException("Check-in can be at most 365 days in advance.");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights > MAX_NIGHTS) {
            throw new IllegalArgumentException("A stay cannot be longer than 30 nights.");
        }
    }

    private Optional<Room> findRoom(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                return Optional.of(room);
            }
        }
        return Optional.empty();
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1,
                                 LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
