package hotel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CancellationPolicy")
class CancellationPolicyTest {

    private final CancellationPolicy policy = new CancellationPolicy();
    // check-in time is 2026-12-10 14:00
    private final LocalDate checkIn = LocalDate.of(2026, 12, 10);

    /*
     * Partitions: [>=48h] -> 100%, [24h, 48h) -> 50%, (0, 24h) -> 0%, [<=0] -> not allowed.
     * Values are chosen on and next to each boundary.
     */
    @ParameterizedTest(name = "TC-C01 cancel at {0} -> {1}% refund")
    @CsvSource({
            "2026-12-01T10:00, 100",   // far before (EP)
            "2026-12-08T13:59, 100",   // 48h + 1 min
            "2026-12-08T14:00, 100",   // exactly 48h (boundary)
            "2026-12-08T14:01, 50",    // 47h 59 min
            "2026-12-09T02:00, 50",    // 36h (EP)
            "2026-12-09T13:59, 50",    // 24h + 1 min
            "2026-12-09T14:00, 50",    // exactly 24h (boundary)
            "2026-12-09T14:01, 0",     // 23h 59 min
            "2026-12-10T13:59, 0"      // 1 min before check-in
    })
    void refundPercentAroundBoundaries(LocalDateTime now, int expected) {
        assertEquals(expected, policy.refundPercent(now, checkIn));
    }

    @ParameterizedTest(name = "TC-C02 cancel at {0} is not allowed")
    @CsvSource({"2026-12-10T14:00", "2026-12-10T14:01", "2026-12-12T09:00"})
    void cannotCancelAtOrAfterCheckIn(LocalDateTime now) {
        assertThrows(IllegalStateException.class, () -> policy.refundPercent(now, checkIn));
    }

    @Test
    @DisplayName("TC-C03 refund amount: 100%, 50% and 0% of 333.33")
    void refundAmount() {
        BigDecimal paid = new BigDecimal("333.33");
        assertEquals(new BigDecimal("333.33"),
                policy.refundAmount(paid, LocalDateTime.of(2026, 12, 1, 10, 0), checkIn));
        assertEquals(new BigDecimal("166.67"),   // 166.665 rounded HALF_UP
                policy.refundAmount(paid, LocalDateTime.of(2026, 12, 9, 2, 0), checkIn));
        assertEquals(new BigDecimal("0.00"),
                policy.refundAmount(paid, LocalDateTime.of(2026, 12, 10, 9, 0), checkIn));
    }

    @Test
    @DisplayName("TC-C04 null arguments are rejected")
    void nullArguments() {
        LocalDateTime now = LocalDateTime.of(2026, 12, 1, 10, 0);
        assertThrows(IllegalArgumentException.class, () -> policy.refundPercent(null, checkIn));
        assertThrows(IllegalArgumentException.class, () -> policy.refundPercent(now, null));
        assertThrows(IllegalArgumentException.class, () -> policy.refundAmount(null, now, checkIn));
    }
}
