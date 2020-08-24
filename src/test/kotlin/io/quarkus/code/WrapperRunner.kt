package io.quarkus.code

import io.quarkus.deployment.util.ProcessUtil
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit

object WrapperRunner {
    enum class Wrapper(private val execUnix: String, private val execWindows: String, val cmdArgs: Array<String>) {
        GRADLE("gradlew", "gradlew.bat", arrayOf<String>("--no-daemon", "build")),
        MAVEN("mvnw", "mvnw.cmd", arrayOf<String>("package"));

        val exec: String
            get() = if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows")) execWindows else execUnix

        companion object {
            fun fromBuildtool(buildtool: String): Wrapper {
                return when (buildtool) {
                    "maven" -> MAVEN
                    "gradle", "gradle-kotlin-dsl" -> GRADLE
                    else -> throw IllegalStateException("No wrapper linked to buildtool: $buildtool")
                }
            }
        }
    }


    fun run(projectDir: Path, wrapper: Wrapper): Int {
        val command: MutableList<String> = LinkedList()
        command.add(projectDir.resolve(wrapper.exec).toAbsolutePath().toString())
        command.addAll(Arrays.asList(*wrapper.cmdArgs))
        try {
            println("Running command: $command")
            val p = ProcessBuilder()
                    .directory(projectDir.toFile())
                    .command(command)
                    .start()
            try {
                ProcessUtil.streamToSysOutSysErr(p)
                p.waitFor(10, TimeUnit.MINUTES)
                return p.exitValue()
            } catch (e: InterruptedException) {
                p.destroyForcibly()
                Thread.currentThread().interrupt()
            }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
        return -1
    }

}
