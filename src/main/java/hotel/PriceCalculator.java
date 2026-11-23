package hotel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Calculates the total price of a stay.
 *
 * Rules:
 *  P1  Friday and Saturday nights cost 20% more than the nightly price
 *  P2  long-stay discount: 7-13 nights = 10%, 14 or more nights = 15%
 *  P3  loyalty members get an extra 10% discount
 *  P4  the combined discount can never be more than 20%
 *  P5  the final total is rounded to 2 decimals (HALF_UP)
 */
public class PriceCalculator {
    public static final BigDecimal WEEKEND_SURCHARGE = new BigDecimal("1.20");
    public static final int MEDIUM_STAY_NIGHTS = 7;
    public static final int LONG_STAY_NIGHTS = 14;
    public static final int MEDIUM_STAY_DISCOUNT = 10;
    public static final int LONG_STAY_DISCOUNT = 15;
    public static final int LOYALTY_DISCOUNT = 10;
    public static final int MAX_TOTAL_DISCOUNT = 20;

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public BigDecimal calculateTotal(BigDecimal pricePerNight, LocalDate checkIn,
                                     LocalDate checkOut, boolean loyaltyMember) {
        if (pricePerNight == null || checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Price and dates cannot be null.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        int nights = 0;
        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            subtotal = subtotal.add(nightlyRate(pricePerNight, night));
            nights++;
        }

        int discount = discountPercent(nights, loyaltyMember);
        BigDecimal multiplier = HUNDRED.subtract(BigDecimal.valueOf(discount)).divide(HUNDRED);
        return subtotal.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    /** Price of one night, including the weekend surcharge (P1). */
    public BigDecimal nightlyRate(BigDecimal pricePerNight, LocalDate night) {
        if (isWeekendNight(night)) {
            return pricePerNight.multiply(WEEKEND_SURCHARGE);
        }
        return pricePerNight;
    }

    /** Total discount in percent (P2, P3, P4). */
    public int discountPercent(int nights, boolean loyaltyMember) {
        if (nights < 1) {
            throw new IllegalArgumentException("Number of nights must be at least 1.");
        }
        int discount = 0;
        if (nights >= LONG_STAY_NIGHTS) {
            discount = LONG_STAY_DISCOUNT;
        } else if (nights >= MEDIUM_STAY_NIGHTS) {
            discount = MEDIUM_STAY_DISCOUNT;
        }
        if (loyaltyMember) {
            discount += LOYALTY_DISCOUNT;
        }
        return Math.min(discount, MAX_TOTAL_DISCOUNT);
    }

    private boolean isWeekendNight(LocalDate night) {
        DayOfWeek day = night.getDayOfWeek();
        return day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY;
    }
}
