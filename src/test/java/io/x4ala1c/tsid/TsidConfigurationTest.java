package io.x4ala1c.tsid;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;

final class TsidConfigurationTest {

    private final TsidConfiguration configuration;

    TsidConfigurationTest() {
        this.configuration = new TsidConfiguration(69, 69420);
    }

    @Test
    void testBuilder() {
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> TsidConfiguration.builder()
                        .node(-1)
                        .build());
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> TsidConfiguration.builder()
                        .node(1024)
                        .build());
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> TsidConfiguration.builder()
                        .epoch(-1)
                        .build());
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> TsidConfiguration.builder()
                        .epoch(2199023255553L)
                        .build());
        Assertions.assertThatNoException()
                .isThrownBy(() -> TsidConfiguration.builder()
                        .node(0)
                        .epoch(0)
                        .build());
        Assertions.assertThatNoException()
                .isThrownBy(() -> TsidConfiguration.builder()
                        .node(1023)
                        .epoch(2199023255552L)
                        .build());
    }

    @Test
    void testGetNode() {
        Assertions.assertThat(configuration.getNode()).isEqualTo(69);
    }

    @Test
    void testGetEpoch() {
        Assertions.assertThat(configuration.getEpoch()).isEqualTo(69420);
    }

    @Test
    @SetEnvironmentVariable(key = "TSID_NODE", value = "96")
    @SetEnvironmentVariable(key = "TSID_EPOCH", value = "96024")
    void testConfigurationFromEnvironment() {
        final TsidConfiguration configurationFromEnv = TsidConfiguration.fromEnvironment();
        Assertions.assertThat(configurationFromEnv.getNode()).isEqualTo(96);
        Assertions.assertThat(configurationFromEnv.getEpoch()).isEqualTo(96024);
    }

    @Test
    @SetSystemProperty(key = "tsid.node", value = "4")
    @SetSystemProperty(key = "tsid.epoch", value = "13")
    @SetEnvironmentVariable(key = "TSID_NODE", value = "96")
    @SetEnvironmentVariable(key = "TSID_EPOCH", value = "96024")
    void testConfigurationFromSystem() {
        final TsidConfiguration configurationFromEnv = TsidConfiguration.fromEnvironment();
        Assertions.assertThat(configurationFromEnv.getNode()).isEqualTo(4);
        Assertions.assertThat(configurationFromEnv.getEpoch()).isEqualTo(13);
    }

    @Test
    @SetEnvironmentVariable(key = "TSID_NODE", value = "")
    void testInvalidNodeConfigurationFromEnv() {
        Assertions.assertThatException().isThrownBy(TsidConfiguration::fromEnvironment);
    }

    @Test
    @SetSystemProperty(key = "tsid.node", value = "")
    void testInvalidNodeConfigurationFromSystem() {
        Assertions.assertThatException().isThrownBy(TsidConfiguration::fromEnvironment);
    }

    @Test
    @SetEnvironmentVariable(key = "TSID_EPOCH", value = "")
    void testInvalidEpochConfigurationFromEnv() {
        Assertions.assertThatException().isThrownBy(TsidConfiguration::fromEnvironment);
    }

    @Test
    @SetSystemProperty(key = "tsid.epoch", value = "")
    void testInvalidEpochConfigurationFromSystem() {
        Assertions.assertThatException().isThrownBy(TsidConfiguration::fromEnvironment);
    }
}
