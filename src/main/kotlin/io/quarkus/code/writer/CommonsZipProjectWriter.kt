package io.quarkus.code.writer

import io.quarkus.cli.commands.writer.ProjectWriter
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipExtraField
import java.io.File
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * ProjectWriter implementation to create a zip.
 */
class CommonsZipProjectWriter private constructor(
    private val aos: ArchiveOutputStream,
    private val basePath: String
) : ProjectWriter {
    private val createdDirs: MutableSet<String> = HashSet()
    private val archiveEntriesByPath: MutableMap<String, ZipArchiveEntry> = LinkedHashMap()
    private val contentByPath: MutableMap<String, ByteArray> = LinkedHashMap()

    override fun write(path: String, content: String) {
        val contentBytes = content.toByteArray(StandardCharsets.UTF_8)
        this.write(path, contentBytes, false)
    }

    fun write(path: String, contentBytes: ByteArray, allowExec: Boolean) {
        val filePath = Paths.get(basePath, "/", path)
        val ze = ZipArchiveEntry(filePath.toString()).apply {
            unixMode = if (allowExec) {
                33261
            } else {
                33188
            }
            extraFields = arrayOf<ZipExtraField>(timestamp)

        }
        archiveEntriesByPath[filePath.toString()] = ze
        contentByPath[filePath.toString()] = contentBytes
    }

    override fun mkdirs(path: String): String =
        if (path.isEmpty()) "" else Paths.get(path).toString()

    private fun mkdirs(dirPath: Path?) {
        if (dirPath == null) {
            return
        }
        mkdirs(dirPath.parent)
        val dirPathText = dirPath.toString()
        if (!createdDirs.contains(dirPathText)) {
            val ze = ZipArchiveEntry("$dirPathText/").apply {
                unixMode = 16877
                extraFields = arrayOf<ZipExtraField>(timestamp)
            }
            aos.putArchiveEntry(ze)
            aos.closeArchiveEntry()
            createdDirs.add(dirPathText)
        }
    }

    override fun getContent(path: String): ByteArray =
        contentByPath[Paths.get(basePath, "/", path).toString()]!!

    override fun exists(path: String): Boolean =
        contentByPath.containsKey(Paths.get(basePath, "/", path).toString())

    override fun getProjectFolder(): File = throw UnsupportedOperationException()

    override fun hasFile(): Boolean = false

    override fun close() {
        aos.use {
            for ((key, value) in contentByPath) {
                val ze = archiveEntriesByPath[key]!!
                this.mkdirs(Paths.get(key).parent)
                aos.putArchiveEntry(ze)
                aos.write(value)
                aos.closeArchiveEntry()
            }
        }
    }

    private val timestamp: X5455_ExtendedTimestamp
        get() = X5455_ExtendedTimestamp().apply {
            createJavaTime = CREATION
            modifyJavaTime = CREATION
            accessJavaTime = CREATION
        }

    companion object {
        // Remove 1 day to be sure the date won't be in the past in any location
        private val CREATION = Date(System.currentTimeMillis() - 24 * 3600000)

        fun createWriter(outputStream: OutputStream?, basePath: String): CommonsZipProjectWriter {
            val aos = ArchiveStreamFactory().createArchiveOutputStream(
                ArchiveStreamFactory.ZIP, outputStream
            )
            return CommonsZipProjectWriter(aos, basePath)
        }
    }

}