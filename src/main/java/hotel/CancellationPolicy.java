package hotel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Decides how much money is refunded when a reservation is cancelled.
 * Check-in time is 14:00 on the check-in date.
 *
 * Rules:
 *  C1  48 hours or more before check-in      -> 100% refund
 *  C2  24 hours or more but less than 48     -> 50% refund
 *  C3  less than 24 hours before check-in    -> no refund
 *  C4  at or after check-in time              -> cancellation not allowed
 */
public class CancellationPolicy {
    public static final LocalTime CHECK_IN_TIME = LocalTime.of(14, 0);
    public static final long FULL_REFUND_HOURS = 48;
    public static final long HALF_REFUND_HOURS = 24;

    /** Refund percentage (100, 50 or 0) for a cancellation made at {@code now}. */
    public int refundPercent(LocalDateTime now, LocalDate checkIn) {
        if (now == null || checkIn == null) {
            throw new IllegalArgumentException("Time and check-in date cannot be null.");
        }
        LocalDateTime checkInTime = checkIn.atTime(CHECK_IN_TIME);
        if (!now.isBefore(checkInTime)) {
            throw new IllegalStateException("Reservation cannot be cancelled at or after check-in time.");
        }
        long minutesLeft = Duration.between(now, checkInTime).toMinutes();
        if (minutesLeft >= FULL_REFUND_HOURS * 60) {
            return 100;
        }
        if (minutesLeft >= HALF_REFUND_HOURS * 60) {
            return 50;
        }
        return 0;
    }

    public BigDecimal refundAmount(BigDecimal paid, LocalDateTime now, LocalDate checkIn) {
        if (paid == null) {
            throw new IllegalArgumentException("Paid amount cannot be null.");
        }
        int percent = refundPercent(now, checkIn);
        return paid.multiply(BigDecimal.valueOf(percent))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
