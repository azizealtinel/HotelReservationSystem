package hotel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HotelReservationSystem {
    private final List<Room> rooms = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();

    public void addRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        if (findRoom(room.getRoomNumber()) != null) {
            throw new IllegalArgumentException("Room number already exists.");
        }
        rooms.add(room);
    }

    public Reservation makeReservation(String guestName, int roomNumber,
                                        LocalDate checkIn, LocalDate checkOut,
                                        int guestCount) {
        Room room = findRoom(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room does not exist.");
        }
        if (guestName == null || guestName.isBlank()) {
            throw new IllegalArgumentException("Guest name cannot be blank.");
        }
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Dates cannot be null.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        if (guestCount < 1 || guestCount > room.getCapacity()) {
            throw new IllegalArgumentException("Guest count exceeds room capacity or is invalid.");
        }
        if (!isRoomAvailable(roomNumber, checkIn, checkOut)) {
            throw new IllegalStateException("Room is not available for the selected dates.");
        }

        Reservation reservation = new Reservation(
                guestName, roomNumber, checkIn, checkOut, guestCount);
        reservations.add(reservation);
        return reservation;
    }

    public boolean isRoomAvailable(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        if (findRoom(roomNumber) == null) {
            return false;
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return false;
        }

        for (Reservation reservation : reservations) {
            if (reservation.getRoomNumber() == roomNumber
                    && datesOverlap(checkIn, checkOut,
                    reservation.getCheckIn(), reservation.getCheckOut())) {
                return false;
            }
        }
        return true;
    }

    public double calculateCost(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        Room room = findRoom(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room does not exist.");
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Invalid stay dates.");
        }
        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        return nights * room.getPricePerNight();
    }

    public int getReservationCount() {
        return reservations.size();
    }

    public List<Room> getRooms() {
        return List.copyOf(rooms);
    }

    private Room findRoom(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                return room;
            }
        }
        return null;
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1,
                                 LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
