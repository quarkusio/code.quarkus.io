package io.launcher.quarkus

import io.launcher.quarkus.model.QuarkusProject
import io.quarkus.test.Mock
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton

@Mock
@Singleton
open class QuarkusProjectCreatorMock: QuarkusProjectCreator() {

    val createdProjectRef : AtomicReference<QuarkusProject> = AtomicReference()

    override fun create(project: QuarkusProject): ByteArray {
        createdProjectRef.set(project)
        return super.create(project)
    }

    fun getCreatedProject(): QuarkusProject {
        return this.createdProjectRef.get()
    }

}