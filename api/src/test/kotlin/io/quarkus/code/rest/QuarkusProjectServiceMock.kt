package io.quarkus.code.rest

import io.quarkus.code.service.QuarkusProjectService
import io.quarkus.code.model.ProjectDefinition
import io.quarkus.code.service.PlatformInfo
import io.quarkus.test.Mock
import java.util.concurrent.atomic.AtomicReference
import jakarta.inject.Singleton

@Mock
@Singleton
open class QuarkusProjectServiceMock: QuarkusProjectService() {

    val createdProjectRef : AtomicReference<ProjectDefinition> = AtomicReference()

    override fun create(platformInfo: PlatformInfo, projectDefinition: ProjectDefinition): ByteArray {
        createdProjectRef.set(projectDefinition)
        return super.create(platformInfo, projectDefinition)
    }

    fun getCreatedProject(): ProjectDefinition {
        return this.createdProjectRef.get()
    }

}