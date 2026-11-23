package hotel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * One reservation. Reservations are created only by {@link HotelReservationSystem},
 * which checks the business rules before creating them.
 */
public class Reservation {

    public enum Status { ACTIVE, CANCELLED }

    private final String id;
    private final Guest guest;
    private final int roomNumber;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int guestCount;
    private final BigDecimal totalPrice;
    private Status status = Status.ACTIVE;
    private BigDecimal refundAmount = BigDecimal.ZERO;

    Reservation(String id, Guest guest, int roomNumber, LocalDate checkIn,
                LocalDate checkOut, int guestCount, BigDecimal totalPrice) {
        this.id = id;
        this.guest = guest;
        this.roomNumber = roomNumber;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestCount = guestCount;
        this.totalPrice = totalPrice;
    }

    void cancel(BigDecimal refund) {
        if (status == Status.CANCELLED) {
            throw new IllegalStateException("Reservation is already cancelled.");
        }
        this.status = Status.CANCELLED;
        this.refundAmount = refund;
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public long getNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public String getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public Status getStatus() {
        return status;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
}
