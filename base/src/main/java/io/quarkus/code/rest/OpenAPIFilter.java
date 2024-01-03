package io.quarkus.code.rest;

import io.quarkus.code.service.PlatformService;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;

import jakarta.enterprise.inject.spi.CDI;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates Enums for Endpoint parameters
 */
public class OpenAPIFilter implements OASFilter {

    @Override
    public PathItem filterPathItem(PathItem pathItem) {
        if (pathItem.getGET() != null && pathItem.getGET().getOperationId() != null) {
            if (pathItem.getGET().getOperationId().equalsIgnoreCase("extensionsForStream")) {
                List<org.eclipse.microprofile.openapi.models.parameters.Parameter> parameters = pathItem.getGET()
                        .getParameters();
                for (org.eclipse.microprofile.openapi.models.parameters.Parameter parameter : parameters) {
                    if (parameter.getName().equalsIgnoreCase("streamKey")) {
                        Schema schema = parameter.getSchema();
                        schema.setEnumeration(new ArrayList<>(getValidStreamValues()));
                    }
                }
            } else if (pathItem.getGET().getOperationId().equalsIgnoreCase("downloadForStream")) {
                List<org.eclipse.microprofile.openapi.models.parameters.Parameter> parameters = pathItem.getGET()
                        .getParameters();
                for (org.eclipse.microprofile.openapi.models.parameters.Parameter parameter : parameters) {
                    if (parameter.getName().equalsIgnoreCase("S")) {
                        Schema schema = parameter.getSchema();
                        schema.setEnumeration(new ArrayList<>(getValidStreamValues()));
                    }
                }
            }
        }
        return pathItem;
    }

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        Schema projectDef = openAPI.getComponents().getSchemas().get("ProjectDefinition");

        if (projectDef != null) {
            Schema streamKey = projectDef.getProperties().get("streamKey");
            if (streamKey != null) {
                streamKey.setEnumeration(new ArrayList<>(getValidStreamValues()));
            }
        }
    }

    private Set<String> getValidStreamValues() {
        try {
            PlatformService platformService = CDI.current().select(PlatformService.class).get();
            return platformService.streamKeys();
        } catch (Exception e) {
            return Set.of();
        }
    }
}