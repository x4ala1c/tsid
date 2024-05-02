package io.github.x4ala1c.tsid;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

final class TsidTest {

    private final long tsidValueLong;
    private final String tsidValueString;

    /**
     * The Tsid will have value of 175928847299117063L and its Crockford's presentation is 09RGCPP108007.
     */
    private final Tsid tsidToTest;

    public TsidTest() {
        this.tsidValueLong = 175928847299117063L;
        this.tsidValueString = "09RGCPP108007";
        this.tsidToTest = new Tsid(tsidValueLong);
    }

    @Test
    void testTsidConstructor() {
        Assertions.assertThatNoException().isThrownBy(() -> new Tsid(0));
        Assertions.assertThatNoException().isThrownBy(() -> new Tsid(1));
        Assertions.assertThatNoException().isThrownBy(() -> new Tsid(Long.MAX_VALUE));
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> new Tsid(-1));
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> new Tsid(Long.MIN_VALUE));
    }

    @Test
    @SuppressWarnings("all")
    void testTsidFromLong() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            final Tsid id = Tsid.fromLong(0);
            Assertions.assertThat(id).isNotNull();
        });
        Assertions.assertThatNoException().isThrownBy(() -> {
            final Tsid id = Tsid.fromLong(1);
            Assertions.assertThat(id).isNotNull();
        });
        Assertions.assertThatNoException().isThrownBy(() -> {
            final Tsid id = Tsid.fromLong(Long.MAX_VALUE);
            Assertions.assertThat(id).isNotNull();
        });
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Tsid.fromLong(-1));
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Tsid.fromLong(Long.MIN_VALUE));
        Assertions.assertThat(Tsid.fromLong(tsidValueLong)).isEqualTo(tsidToTest);
    }

    @Test
    @SuppressWarnings("all")
    void testTsidFromString() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> Tsid.fromString(null));
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Tsid.fromString(""));
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Tsid.fromString(" "));
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Tsid.fromString("ABC"));
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Tsid.fromString("ABCDEFGHIJKLMN"));
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Tsid.fromString("ABCD$FGHIJKLM"));
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Tsid.fromString("ABCDEFGHIJKL%"));
        Assertions.assertThatNoException().isThrownBy(() -> Tsid.fromString("ABCDEFGHIJKLM"));
        Assertions.assertThatNoException().isThrownBy(() -> Tsid.fromString("abcdefghijklm"));
        Assertions.assertThat(Tsid.fromString(tsidValueString)).isEqualTo(tsidToTest);
    }

    @Test
    void testTsidAsLong() {
        Assertions.assertThat(tsidToTest.asLong()).isEqualTo(tsidValueLong);
    }

    @Test
    void testTsidAsString() {
        Assertions.assertThat(tsidToTest.asString()).isEqualTo(tsidValueString);
    }

    @Test
    void testTsidToString() {
        Assertions.assertThat(tsidToTest.toString()).hasToString(tsidValueString);
    }
}
