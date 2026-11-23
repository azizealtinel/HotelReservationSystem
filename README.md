# Hotel Reservation System

Java 17 + Maven + JUnit 5 project for the SVV research assignment.
No GUI and no database: rooms and reservations are kept in memory.

## Project structure

```
src/main/java/hotel/
  Room.java                    room number, capacity, nightly price (rules R1-R4)
  Guest.java                   guest name and loyalty membership (rules G1-G3)
  PriceCalculator.java         weekend surcharge, discounts, rounding (rules P1-P5)
  CancellationPolicy.java      refund rules (rules C1-C4)
  Reservation.java             one reservation (ACTIVE / CANCELLED)
  HotelReservationSystem.java  rooms, reservations, availability (rules B1-B9)
  Main.java                    small console demo
src/test/java/hotel/
  RoomTest, GuestTest, PriceCalculatorTest, CancellationPolicyTest,
  HotelReservationSystemTest, DefectRegressionTest
docs/
  test-runs/                   saved test outputs and mutation analysis
  v1-defect-tests/             the defect tests rewritten for the first version
```

## Business rules

| ID | Rule |
|----|------|
| R1 | Room number is between 1 and 9999 |
| R2 | Room capacity is between 1 and 6 |
| R3 | Nightly price is between 0.01 and 100000.00 |
| R4 | Nightly price has at most 2 decimal places |
| G1 | Guest name cannot be null or blank |
| G2 | Guest name length (trimmed) is between 2 and 50 |
| G3 | Guest name contains only letters (incl. Turkish), spaces, `-` and `'` |
| B1 | The room must exist |
| B2 | Check-out must be after check-in |
| B3 | Check-in cannot be in the past |
| B4 | Check-in is at most 365 days in advance |
| B5 | A stay is between 1 and 30 nights |
| B6 | Guest count is between 1 and the room capacity |
| B7 | No overlapping active reservations for the same room (a stay may start on the day another ends) |
| B8 | Cancelled reservations do not block the room |
| B9 | Room numbers are unique |
| P1 | Friday and Saturday nights cost 20% more |
| P2 | 7-13 nights: 10% discount, 14+ nights: 15% discount |
| P3 | Loyalty members get an extra 10% discount |
| P4 | Total discount is at most 20% |
| P5 | Total is rounded to 2 decimals (HALF_UP) |
| C1 | Cancel 48h or more before check-in (14:00): 100% refund |
| C2 | Cancel 24h-48h before: 50% refund |
| C3 | Cancel less than 24h before: no refund |
| C4 | Cancelling at or after check-in time is not allowed |

## Run

```bash
mvn clean test                    # all tests + JaCoCo coverage report
mvn test -Dgroups=regression      # only the regression tests (DEF-01..04)
mvn compile exec:java -Dexec.mainClass=hotel.Main   # demo (or run Main in IntelliJ)
```

Coverage report: `target/site/jacoco/index.html`
Surefire test reports: `target/surefire-reports/`

## IntelliJ

Open the folder that contains `pom.xml`. Right-click `src/test/java` → **Run 'All Tests'**.
For coverage: right-click `src/test/java` → **More Run/Debug → Run 'All Tests' with Coverage**.
