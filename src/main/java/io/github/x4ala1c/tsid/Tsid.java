package io.github.x4ala1c.tsid;

import java.util.Objects;

/**
 * TSID (Time-Sorted ID) is a type of ID that balances well between the look of the UUID and its ability to support
 * indexing for the database.
 * <p>
 * Its value is a long value (64-bit signed integer) and consists of 3 parts, in order:
 * <ol>
 * <li> 42 bits of timestamp (1 sign bit + 41 timestamp bits), which is the difference between the epoch (customizable)
 * and the ID's creation time. The timestamp is in milliseconds.
 * <li> 10-bit representation of the node or machine that generates the ID. This reduces the ID's collision across the
 * multi-node system (i.e., microservices, etc.).
 * <li> 12-bit sequence for cases when multiple IDs are generated in the same millisecond. The starting of the
 * sequence is securely randomized.
 * </ol>
 * <p>
 * The String form of the Tsid is in <a href="https://www.crockford.com/base32.html">Crockford's Base32</a>.
 */
public final class Tsid implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    static final byte MAX_STRING_LENGTH = Long.SIZE / 5 + 1;

    private final long value;

    Tsid(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value is below 0");
        }
        this.value = value;
    }

    /**
     * Gets {@link Tsid} instance from {@code long} value. Must be non-negative.
     *
     * @param value The value of {@link Tsid} as long.
     *
     * @return {@link Tsid} instance.
     */
    public static Tsid fromLong(long value) {
        return new Tsid(value);
    }

    /**
     * Gets {@link Tsid} instance from {@code String} value. Must have length of 13 (after trimmed) and part of
     * Crockford's Base32 characters.
     *
     * @param value The value of {@link Tsid} in Crockford's Base32 encoding.
     *
     * @return {@link Tsid} instance.
     */
    public static Tsid fromString(String value) {
        if (value == null) {
            throw new NullPointerException("Value is null");
        }
        final String trimmedInput = value.trim();
        if (trimmedInput.length() != MAX_STRING_LENGTH) {
            throw new IllegalArgumentException("Value must be " + MAX_STRING_LENGTH + " characters long");
        }
        final long decodedValue = CrockfordCodec.decode(trimmedInput);
        return new Tsid(decodedValue);
    }

    /**
     * Returns the value of {@link Tsid} as {@code long}.
     *
     * @return {@code long} representation of the current {@link Tsid}.
     */
    public long asLong() {
        return value;
    }

    /**
     * Returns the value of {@link Tsid} as {@code String} in Crockford's Base32 encoding.
     *
     * @return {@code String} representation of the current {@link Tsid}, in Crockford's Base32 encoding.
     */
    public String asString() {
        return CrockfordCodec.encode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tsid tsid = (Tsid) o;
        return value == tsid.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return asString();
    }
}
