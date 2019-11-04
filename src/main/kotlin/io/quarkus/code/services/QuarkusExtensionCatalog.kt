package io.quarkus.code.services

import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.platform.descriptor.resolver.json.QuarkusJsonPlatformDescriptorResolver
import io.quarkus.runtime.StartupEvent
import org.eclipse.microprofile.config.spi.ConfigProviderResolver
import java.util.concurrent.atomic.AtomicBoolean
import javax.enterprise.event.Observes
import javax.inject.Singleton

@Singleton
open class QuarkusExtensionCatalog {

    companion object {
        @JvmStatic
        val platformVersion = ConfigProviderResolver.instance().getConfig().getValue("io.quarkus.code.quarkus-platform-version", String::class.java)

        @JvmStatic
        val descriptor = QuarkusJsonPlatformDescriptorResolver.newInstance().resolveFromBom("io.quarkus", "quarkus-universe-bom", platformVersion)

    }

    lateinit var extensions: List<CodeQuarkusExtension>
    private val loaded = AtomicBoolean(false)

    fun onStart(@Observes ev: StartupEvent) {
        initExtensions()
    }

    fun isLoaded(): Boolean {
        return loaded.get()
    }

    private fun initExtensions() {
        this.extensions = QuarkusExtensionUtils.processExtensions(descriptor)
        loaded.set(true)
    }


}