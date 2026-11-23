package hotel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PriceCalculator")
class PriceCalculatorTest {

    private final PriceCalculator calc = new PriceCalculator();
    private final BigDecimal price100 = new BigDecimal("100.00");
    // 2026-12-07 is a Monday
    private final LocalDate monday = LocalDate.of(2026, 12, 7);

    @Nested
    @DisplayName("Weekend surcharge (P1)")
    class Weekend {

        @Test
        @DisplayName("TC-P01 three weekday nights have no surcharge")
        void weekdayNights() {
            assertEquals(new BigDecimal("300.00"),
                    calc.calculateTotal(price100, monday, monday.plusDays(3), false));
        }

        @Test
        @DisplayName("TC-P02 Thursday-Sunday: Friday and Saturday cost 20% more")
        void weekendNights() {
            LocalDate thursday = monday.plusDays(3);
            assertEquals(new BigDecimal("340.00"),
                    calc.calculateTotal(price100, thursday, thursday.plusDays(3), false));
        }

        @ParameterizedTest(name = "TC-P03 night on {0} costs {1}")
        @CsvSource({
                "2026-12-10, 100.00",   // Thursday
                "2026-12-11, 120.00",   // Friday
                "2026-12-12, 120.00",   // Saturday
                "2026-12-13, 100.00"    // Sunday
        })
        void nightlyRateByDay(LocalDate night, BigDecimal expected) {
            assertEquals(0, expected.compareTo(calc.nightlyRate(price100, night)));
        }
    }

    @Nested
    @DisplayName("Discounts (P2-P4) - decision table + BVA")
    class Discounts {

        /*
         * Decision table
         * Stay length   | <7 | <7  | 7-13 | 7-13 | >=14 | >=14
         * Loyalty member|  N |  Y  |  N   |  Y   |  N   |  Y
         * ------------- |----|-----|------|------|------|------
         * Discount %    |  0 | 10  | 10   | 20   | 15   | 20 (capped from 25)
         */
        @ParameterizedTest(name = "TC-P04 nights={0}, loyalty={1} -> {2}%")
        @CsvSource({
                "1,  false, 0",
                "6,  false, 0",
                "1,  true,  10",
                "6,  true,  10",
                "7,  false, 10",
                "13, false, 10",
                "7,  true,  20",
                "13, true,  20",
                "14, false, 15",
                "30, false, 15",
                "14, true,  20",
                "30, true,  20"
        })
        void decisionTable(int nights, boolean loyalty, int expectedPercent) {
            assertEquals(expectedPercent, calc.discountPercent(nights, loyalty));
        }

        @ParameterizedTest(name = "TC-P05 invalid number of nights {0}")
        @ValueSource(ints = {0, -1})
        void rejectsZeroOrNegativeNights(int nights) {
            assertThrows(IllegalArgumentException.class, () -> calc.discountPercent(nights, false));
        }

        @ParameterizedTest(name = "TC-P06 BVA {0} nights from Monday -> {1}")
        @CsvSource({
                "6,  640.00",    // 4x100 + 2x120, no discount
                "7,  666.00",    // 740 * 0.90
                "13, 1242.00",   // 1380 * 0.90
                "14, 1258.00"    // 1480 * 0.85
        })
        void totalAroundDiscountBoundaries(int nights, BigDecimal expected) {
            assertEquals(expected, calc.calculateTotal(price100, monday, monday.plusDays(nights), false));
        }
    }

    @Nested
    @DisplayName("Rounding (P5)")
    class Rounding {

        @Test
        @DisplayName("TC-P07 50.05 x 3 nights is exactly 150.15")
        void exactDecimalArithmetic() {
            assertEquals(new BigDecimal("150.15"),
                    calc.calculateTotal(new BigDecimal("50.05"), monday, monday.plusDays(3), false));
        }

        @Test
        @DisplayName("TC-P08 half-cent is rounded up (9.045 -> 9.05)")
        void halfUpRounding() {
            assertEquals(new BigDecimal("9.05"),
                    calc.calculateTotal(new BigDecimal("10.05"), monday, monday.plusDays(1), true));
        }

        @Test
        @DisplayName("TC-P09 result always has two decimals")
        void twoDecimals() {
            assertEquals(2, calc.calculateTotal(new BigDecimal("100"), monday, monday.plusDays(1), false).scale());
        }
    }

    @Nested
    @DisplayName("Negative tests")
    class Negative {

        @Test
        @DisplayName("TC-P10 null price or dates are rejected")
        void nullArguments() {
            assertThrows(IllegalArgumentException.class,
                    () -> calc.calculateTotal(null, monday, monday.plusDays(1), false));
            assertThrows(IllegalArgumentException.class,
                    () -> calc.calculateTotal(price100, null, monday.plusDays(1), false));
            assertThrows(IllegalArgumentException.class,
                    () -> calc.calculateTotal(price100, monday, null, false));
        }

        @Test
        @DisplayName("TC-P11 check-out on or before check-in is rejected")
        void invalidDateOrder() {
            assertThrows(IllegalArgumentException.class,
                    () -> calc.calculateTotal(price100, monday, monday, false));
            assertThrows(IllegalArgumentException.class,
                    () -> calc.calculateTotal(price100, monday, monday.minusDays(1), false));
        }
    }
}
