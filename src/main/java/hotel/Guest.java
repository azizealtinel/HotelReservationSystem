package hotel;

/**
 * A hotel guest.
 *
 * Rules:
 *  G1  name cannot be null or blank
 *  G2  name length (after trimming) must be between 2 and 50 characters
 *  G3  name may contain only letters, spaces, hyphens and apostrophes
 *      (Turkish letters such as ç, ğ, ı, ö, ş, ü are allowed)
 */
public class Guest {
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;
    private static final String NAME_PATTERN = "[\\p{L} '\\-]+";

    private final String name;
    private final boolean loyaltyMember;

    public Guest(String name, boolean loyaltyMember) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Guest name cannot be blank.");
        }
        String trimmed = name.trim();
        if (trimmed.length() < MIN_NAME_LENGTH || trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Guest name must be between 2 and 50 characters.");
        }
        if (!trimmed.matches(NAME_PATTERN)) {
            throw new IllegalArgumentException(
                    "Guest name may contain only letters, spaces, hyphens and apostrophes.");
        }
        this.name = trimmed;
        this.loyaltyMember = loyaltyMember;
    }

    public Guest(String name) {
        this(name, false);
    }

    public String getName() {
        return name;
    }

    public boolean isLoyaltyMember() {
        return loyaltyMember;
    }
}
