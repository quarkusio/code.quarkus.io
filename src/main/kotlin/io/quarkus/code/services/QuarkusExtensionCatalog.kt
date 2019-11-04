package io.quarkus.code.services

import com.google.common.base.Preconditions.checkState
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.platform.descriptor.resolver.json.QuarkusJsonPlatformDescriptorResolver
import org.eclipse.microprofile.config.spi.ConfigProviderResolver
import javax.inject.Singleton

@Singleton
open class QuarkusExtensionCatalog {

    companion object {
        @JvmStatic
        val platformVersion = ConfigProviderResolver.instance().getConfig().getValue("io.quarkus.code.quarkus-platform-version", String::class.java)

        @JvmStatic
        val bundledQuarkusVersion =ConfigProviderResolver.instance().getConfig().getValue("io.quarkus.code.quarkus-version", String::class.java)

        @JvmStatic
        val descriptor = QuarkusJsonPlatformDescriptorResolver.newInstance().resolveFromBom("io.quarkus", "quarkus-universe-bom", platformVersion)

        init {
            checkState(descriptor.quarkusVersion == bundledQuarkusVersion, "The platform version (%s) must be compatible with the bundled Quarkus version (%s != %s)", descriptor.bomVersion, descriptor.quarkusVersion, bundledQuarkusVersion)
        }
    }

    val extensions: List<CodeQuarkusExtension> = QuarkusExtensionUtils.processExtensions(descriptor)

}