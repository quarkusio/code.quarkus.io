package io.quarkus.code.services

import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.platform.tools.config.QuarkusPlatformConfig
import io.quarkus.runtime.StartupEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.enterprise.event.Observes
import javax.inject.Singleton

@Singleton
open class QuarkusExtensionCatalog {

    companion object {
        @JvmStatic
        val descriptor = QuarkusPlatformConfig.getGlobalDefault().platformDescriptor
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