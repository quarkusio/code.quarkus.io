package io.quarkus.code.service

import io.quarkus.registry.catalog.PlatformCatalog
import java.time.LocalDateTime

data class PlatformServiceCache(
        var platformCatalog: PlatformCatalog,
        var streamCatalogMap: MutableMap<String, PlatformInfo>,
        var lastUpdated: LocalDateTime
)