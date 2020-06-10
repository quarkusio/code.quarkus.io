package io.quarkus.code.rest

import io.quarkus.code.service.QuarkusProjectService
import io.quarkus.code.model.ProjectDefinition
import io.quarkus.test.Mock
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton

@Mock
@Singleton
open class QuarkusProjectServiceMock: QuarkusProjectService() {

    val createdProjectRef : AtomicReference<ProjectDefinition> = AtomicReference()

    override fun create(projectDefinition: ProjectDefinition): ByteArray {
        createdProjectRef.set(projectDefinition)
        return super.create(projectDefinition)
    }

    fun getCreatedProject(): ProjectDefinition {
        return this.createdProjectRef.get()
    }

}