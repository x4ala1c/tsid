package io.x4ala1c.tsid;

import java.util.Objects;

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

    public static Tsid fromLong(long value) {
        return new Tsid(value);
    }

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

    public long asLong() {
        return value;
    }

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
