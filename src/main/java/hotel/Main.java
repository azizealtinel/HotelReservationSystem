package hotel;

import java.time.LocalDate;

/** Small console demo of the reservation system. */
public class Main {
    public static void main(String[] args) {
        HotelReservationSystem hotel = new HotelReservationSystem();
        hotel.addRoom(new Room(101, 2, "100.00"));
        hotel.addRoom(new Room(201, 4, "250.00"));

        LocalDate checkIn = LocalDate.now().plusDays(10);
        Guest guest = new Guest("Ayşe Yılmaz", true);

        Reservation r = hotel.makeReservation(guest, 101, checkIn, checkIn.plusDays(7), 2);
        System.out.println("Reservation " + r.getId() + " for " + r.getGuest().getName()
                + ", room " + r.getRoomNumber() + ", " + r.getNights() + " nights, total "
                + r.getTotalPrice());

        try {
            hotel.makeReservation(new Guest("Mehmet Kaya"), 101, checkIn.plusDays(2), checkIn.plusDays(4), 1);
        } catch (IllegalStateException e) {
            System.out.println("Second booking rejected: " + e.getMessage());
        }

        System.out.println("Refund on cancellation: " + hotel.cancelReservation(r.getId()));
        System.out.println("Active reservations: " + hotel.getActiveReservationCount());
    }
}
