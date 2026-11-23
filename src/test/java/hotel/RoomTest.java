package hotel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Room")
class RoomTest {

    @Nested
    @DisplayName("Room number (R1) - EP + BVA")
    class RoomNumber {

        @ParameterizedTest(name = "TC-RM01 valid room number {0}")
        @ValueSource(ints = {1, 2, 500, 9998, 9999})
        void acceptsValidRoomNumbers(int number) {
            assertEquals(number, new Room(number, 2, "100.00").getRoomNumber());
        }

        @ParameterizedTest(name = "TC-RM02 invalid room number {0}")
        @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 10000, Integer.MAX_VALUE})
        void rejectsInvalidRoomNumbers(int number) {
            assertThrows(IllegalArgumentException.class, () -> new Room(number, 2, "100.00"));
        }
    }

    @Nested
    @DisplayName("Capacity (R2) - EP + BVA")
    class Capacity {

        @ParameterizedTest(name = "TC-RM03 valid capacity {0}")
        @ValueSource(ints = {1, 2, 5, 6})
        void acceptsValidCapacity(int capacity) {
            assertEquals(capacity, new Room(101, capacity, "100.00").getCapacity());
        }

        @ParameterizedTest(name = "TC-RM04 invalid capacity {0}")
        @ValueSource(ints = {-1, 0, 7, 100})
        void rejectsInvalidCapacity(int capacity) {
            assertThrows(IllegalArgumentException.class, () -> new Room(101, capacity, "100.00"));
        }
    }

    @Nested
    @DisplayName("Price per night (R3, R4) - EP + BVA")
    class Price {

        @ParameterizedTest(name = "TC-RM05 valid price {0}")
        @ValueSource(strings = {"0.01", "0.02", "100", "100.5", "99999.99", "100000.00"})
        void acceptsValidPrices(String price) {
            Room room = new Room(101, 2, price);
            assertEquals(0, new BigDecimal(price).compareTo(room.getPricePerNight()));
        }

        @ParameterizedTest(name = "TC-RM06 invalid price {0}")
        @ValueSource(strings = {"-100", "-0.01", "0", "0.00", "100000.01", "1E+10"})
        void rejectsPricesOutsideRange(String price) {
            assertThrows(IllegalArgumentException.class, () -> new Room(101, 2, price));
        }

        @ParameterizedTest(name = "TC-RM07 too many decimals {0}")
        @ValueSource(strings = {"10.005", "0.001", "99.999"})
        void rejectsMoreThanTwoDecimals(String price) {
            assertThrows(IllegalArgumentException.class, () -> new Room(101, 2, price));
        }

        @Test
        @DisplayName("TC-RM08 trailing zeros are not counted as extra decimals")
        void acceptsTrailingZeros() {
            assertDoesNotThrow(() -> new Room(101, 2, "10.5000"));
        }

        @Test
        @DisplayName("TC-RM09 null price is rejected")
        void rejectsNullPrice() {
            assertThrows(IllegalArgumentException.class, () -> new Room(101, 2, (BigDecimal) null));
            assertThrows(IllegalArgumentException.class, () -> new Room(101, 2, (String) null));
        }
    }
}
