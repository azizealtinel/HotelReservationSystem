package hotel;

public class Room {
    private final int roomNumber;
    private final int capacity;
    private final double pricePerNight;

    public Room(int roomNumber, int capacity, double pricePerNight) {
        if (roomNumber <= 0) {
            throw new IllegalArgumentException("Room number must be positive.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        if (pricePerNight <= 0) {
            throw new IllegalArgumentException("Price per night must be positive.");
        }
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }
}
