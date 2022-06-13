package io.quarkus.code.rest

import org.eclipse.microprofile.openapi.OASFilter
import org.eclipse.microprofile.openapi.models.PathItem
import io.quarkus.code.service.PlatformService
import org.eclipse.microprofile.openapi.models.OpenAPI
import org.eclipse.microprofile.openapi.models.media.Schema
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
                    if (parameter.name.equals("streamKey", ignoreCase = true)) {
                        val schema = parameter.schema
                        schema.enumeration = ArrayList<Any>(validStreamValues)
                    }
                }
            } else if (pathItem.get.operationId.equals("downloadForStream", ignoreCase = true)) {
                val parameters = pathItem.get.parameters
                for (parameter in parameters) {
                    if (parameter.name.equals("S", ignoreCase = false)) {
                        val schema = parameter.schema
                        schema.enumeration = ArrayList<Any>(validStreamValues)
                    }
                }
            }
        }
        return pathItem
    }

    override fun filterOpenAPI(openAPI: OpenAPI){
        val projectDef: Schema? = openAPI.components.schemas["ProjectDefinition"]

        if (projectDef != null) {
            val streamKey: Schema? = projectDef.properties["streamKey"]
            if (streamKey != null) {
                streamKey.enumeration = ArrayList<Any>(validStreamValues)
            }
        }

    }

    private val validStreamValues: Set<String>
        get() {
            return try {
                val platformService = CDI.current().select(PlatformService::class.java).get()
                platformService.streamKeys
            }catch(e : Exception){
                emptySet()
            }
        }
}