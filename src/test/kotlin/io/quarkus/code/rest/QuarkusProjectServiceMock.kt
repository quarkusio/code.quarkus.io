package io.quarkus.code.rest

import io.quarkus.code.service.QuarkusProjectService
import io.quarkus.code.model.QuarkusProject
import io.quarkus.test.Mock
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton

@Mock
@Singleton
open class QuarkusProjectServiceMock: QuarkusProjectService() {

    val createdProjectRef : AtomicReference<QuarkusProject> = AtomicReference()

    override fun create(project: QuarkusProject): ByteArray {
        createdProjectRef.set(project)
        return super.create(project)
    }

    fun getCreatedProject(): QuarkusProject {
        return this.createdProjectRef.get()
    }

}