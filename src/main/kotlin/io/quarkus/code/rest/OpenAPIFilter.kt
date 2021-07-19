package io.quarkus.code.rest

import org.eclipse.microprofile.openapi.OASFilter
import org.eclipse.microprofile.openapi.models.PathItem
import io.quarkus.code.service.PlatformService
import java.util.ArrayList
import javax.enterprise.inject.spi.CDI

/**
 * Creates Enums for Endpoint parameters
 */
class OpenAPIFilter : OASFilter {
    override fun filterPathItem(pathItem: PathItem): PathItem {
        if (pathItem.get != null && pathItem.get.operationId != null) {
            if (pathItem.get.operationId.equals("extensionsForStream", ignoreCase = true)) {
                val parameters = pathItem.get.parameters
                for (parameter in parameters) {
                    if (parameter.name.equals("stream", ignoreCase = true)) {
                        val schema = parameter.schema
                        schema.enumeration = ArrayList<Any>(validStreamValues)
                    }
                }
            }
        }
        return pathItem
    }

    private val validStreamValues: Set<String>
        get() {
            val platformService = CDI.current().select(PlatformService::class.java).get()
            return platformService.streamKeys
        }
}