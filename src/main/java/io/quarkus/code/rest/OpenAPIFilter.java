package io.quarkus.code.rest;

import io.quarkus.code.service.PlatformService;
import io.quarkus.maven.ArtifactCoords;
import io.quarkus.registry.catalog.Platform;
import io.quarkus.registry.catalog.PlatformRelease;
import io.quarkus.registry.catalog.PlatformStream;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;

import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Creates Enums for Endpoint parameters
 */
public class OpenAPIFilter implements OASFilter {

    @Override
    public PathItem filterPathItem(PathItem pathItem) {
        if(pathItem.getGET()!=null && pathItem.getGET().getOperationId()!=null) {
            if (pathItem.getGET().getOperationId().equalsIgnoreCase("extensionsForStream")) {
                List<Parameter> parameters = pathItem.getGET().getParameters();
                for (Parameter parameter : parameters) {
                    if (parameter.getName().equalsIgnoreCase("stream")) {
                        Schema schema = parameter.getSchema();
                        schema.setEnumeration(new ArrayList<>(getValidStreamValues()));
                    }
                }
            }
        }
        return pathItem;
    }

    private Set<String> getValidStreamValues(){
        PlatformService platformService = CDI.current().select(PlatformService.class).get();
        return platformService.getStreamKeys();
    }
}
