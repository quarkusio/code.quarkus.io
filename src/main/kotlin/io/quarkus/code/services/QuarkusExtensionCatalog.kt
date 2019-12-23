package io.quarkus.code.services

import com.google.common.base.Preconditions.checkState
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.platform.descriptor.resolver.json.QuarkusJsonPlatformDescriptorResolver
import io.quarkus.platform.tools.config.QuarkusPlatformConfig
import org.eclipse.microprofile.config.spi.ConfigProviderResolver
import javax.inject.Singleton

@Singleton
open class QuarkusExtensionCatalog {

    companion object {
        @JvmStatic
        internal val platformVersion = "1.1.0.Final"

        @JvmStatic
        internal val bundledQuarkusVersion = "1.1.0.Final"

        @JvmStatic
        internal val descriptor = QuarkusJsonPlatformDescriptorResolver.newInstance().resolveFromBom("io.quarkus", "quarkus-universe-bom", platformVersion)

        @JvmStatic
        internal val processedExtensions = QuarkusExtensionUtils.processExtensions(descriptor)

        init {
            checkState(descriptor.quarkusVersion == bundledQuarkusVersion, "The platform version (%s) must be compatible with the bundled Quarkus version (%s != %s)", descriptor.bomVersion, descriptor.quarkusVersion, bundledQuarkusVersion)
            if (!QuarkusPlatformConfig.hasGlobalDefault()) {
                QuarkusPlatformConfig.defaultConfigBuilder().setPlatformDescriptor(descriptor).build()
            }
        }

        fun checkPlatformInitialization() {
            check(QuarkusPlatformConfig.hasGlobalDefault()) { "Quarkus platform must be initialized" }
        }
    }

    val extensions = processedExtensions

}
