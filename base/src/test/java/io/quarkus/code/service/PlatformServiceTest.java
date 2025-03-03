package io.quarkus.code.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PlatformServiceTest {

    @Test
    void getStreamStatusTest() {
        assertThat(PlatformService.getStreamStatus("1.0.0.0.Final")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("1.0.0.0-CR1")).isEqualTo("CR1");
        assertThat(PlatformService.getStreamStatus("1.0.0.Final")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("1.0.0-Final")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("1-Final")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("SNAPSHOT")).isEqualTo("SNAPSHOT");
        assertThat(PlatformService.getStreamStatus("")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("1")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("1.0")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("1.0.0")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("1.0.0.0")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("1.0.0.0.Final-redhat-00001")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("1.0.0.0.redhat-00001")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("1.0.0.0.SP1-redhat-00001")).isEqualTo("SP1");
        assertThat(PlatformService.getStreamStatus("1.0.0.0.CR2-redhat-00001")).isEqualTo("CR2");
        assertThat(PlatformService.getStreamStatus("1.0.0-redhat-00001")).isEqualTo("FINAL");
        assertThat(PlatformService.getStreamStatus("1.0.0-SP1-redhat-00001")).isEqualTo("SP1");
        assertThat(PlatformService.getStreamStatus("1.0.0-CR2-redhat-00001")).isEqualTo("CR2");
    }
}