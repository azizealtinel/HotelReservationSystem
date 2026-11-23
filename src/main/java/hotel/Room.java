package hotel;

import java.math.BigDecimal;

/**
 * A hotel room. All fields are validated when the room is created and
 * cannot change afterwards.
 *
 * Rules:
 *  R1  room number must be between 1 and 9999
 *  R2  capacity must be between 1 and 6 guests
 *  R3  nightly price must be between 0.01 and 100000.00
 *  R4  nightly price may have at most two decimal places
 */
public class Room {
    public static final int MIN_ROOM_NUMBER = 1;
    public static final int MAX_ROOM_NUMBER = 9999;
    public static final int MIN_CAPACITY = 1;
    public static final int MAX_CAPACITY = 6;
    public static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
    public static final BigDecimal MAX_PRICE = new BigDecimal("100000.00");

    private final int roomNumber;
    private final int capacity;
    private final BigDecimal pricePerNight;

    public Room(int roomNumber, int capacity, BigDecimal pricePerNight) {
        if (roomNumber < MIN_ROOM_NUMBER || roomNumber > MAX_ROOM_NUMBER) {
            throw new IllegalArgumentException("Room number must be between 1 and 9999.");
        }
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException("Capacity must be between 1 and 6.");
        }
        if (pricePerNight == null) {
            throw new IllegalArgumentException("Price per night cannot be null.");
        }
        if (pricePerNight.compareTo(MIN_PRICE) < 0 || pricePerNight.compareTo(MAX_PRICE) > 0) {
            throw new IllegalArgumentException("Price per night must be between 0.01 and 100000.00.");
        }
        if (pricePerNight.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Price per night can have at most two decimal places.");
        }
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
    }

    /** Convenience constructor, e.g. new Room(101, 2, "100.00"). */
    public Room(int roomNumber, int capacity, String pricePerNight) {
        this(roomNumber, capacity, pricePerNight == null ? null : new BigDecimal(pricePerNight));
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }
}
