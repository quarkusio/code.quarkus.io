package io.quarkus.code.rest;

import io.quarkus.code.service.QuarkusProjectService;
import io.quarkus.code.model.ProjectDefinition;
import io.quarkus.code.service.PlatformInfo;
import io.quarkus.devtools.commands.data.QuarkusCommandException;
import io.quarkus.test.Mock;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.inject.Singleton;

@Mock
@Singleton
public class QuarkusProjectServiceMock extends QuarkusProjectService {

    private AtomicReference<ProjectDefinition> createdProjectRef = new AtomicReference<>();

    @Override
    public byte[] create(PlatformInfo platformInfo, ProjectDefinition projectDefinition)
            throws IOException, QuarkusCommandException {
        createdProjectRef.set(projectDefinition);
        return super.create(platformInfo, projectDefinition);
    }

    public ProjectDefinition getCreatedProject() {
        return this.createdProjectRef.get();
    }

}