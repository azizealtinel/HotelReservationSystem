package hotel;

import java.time.LocalDate;

public class Reservation {
    private final String guestName;
    private final int roomNumber;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int guestCount;

    public Reservation(String guestName, int roomNumber, LocalDate checkIn,
                       LocalDate checkOut, int guestCount) {
        if (guestName == null || guestName.isBlank()) {
            throw new IllegalArgumentException("Guest name cannot be blank.");
        }
        if (roomNumber <= 0) {
            throw new IllegalArgumentException("Room number must be positive.");
        }
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Dates cannot be null.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        if (guestCount <= 0) {
            throw new IllegalArgumentException("Guest count must be positive.");
        }
        this.guestName = guestName.trim();
        this.roomNumber = roomNumber;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestCount = guestCount;
    }

    public String getGuestName() {
        return guestName;
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
}
