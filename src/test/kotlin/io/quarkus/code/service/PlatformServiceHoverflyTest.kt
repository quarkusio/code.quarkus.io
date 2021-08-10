package io.quarkus.code.service

import io.quarkus.code.config.ExtensionProcessorConfig
import io.specto.hoverfly.junit5.HoverflyExtension
import io.specto.hoverfly.junit5.api.HoverflyConfig
import io.specto.hoverfly.junit5.api.HoverflySimulate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@HoverflySimulate(config = HoverflyConfig(destination = ["registry.quarkus.io"], disableTlsVerification = true))
@ExtendWith(HoverflyExtension::class)
class PlatformServiceHoverflyTest {

    val platformService: PlatformService by lazy {
            val ps = PlatformService()
            ps.projectService = QuarkusProjectService()
            ps.extensionProcessorConfig = object : ExtensionProcessorConfig {
                override val tagsFrom: Optional<String>
                    get() = Optional.empty()
            }
            ps
        }

    @Test
    fun testInitialLoad() {
        platformService.onStart(null)
        assertThat(platformService.recommendedStreamKey).isEqualTo("io.quarkus.platform:2.1")
        assertThat(platformService.recommendedPlatformInfo.quarkusCoreVersion).isEqualTo("2.1.1.Final")
        assertThat(platformService.recommendedPlatformInfo.codeQuarkusExtensions).hasSize(464)
    }
}