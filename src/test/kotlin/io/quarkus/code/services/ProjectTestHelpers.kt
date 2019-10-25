package io.quarkus.code.services

import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import java.io.File
import java.nio.file.Files

class ProjectTestHelpers {
    companion object {

        fun readFiles(testDir: File): List<String> {
            return testDir.walkTopDown()
                .map { file -> file.relativeTo(testDir).toString() }
                .toList()
        }

        fun extractProject(proj: ByteArray): Pair<File, List<String>> {
            val testDir = Files.createTempDirectory("test-zip").toFile()
            println(testDir)
            val zipFile = testDir.resolve("project.zip")
            zipFile.outputStream().use { output ->
                output.write(proj)
            }
            val zipList = unzip(testDir, zipFile)
            return Pair(testDir, zipList)
        }

        fun unzip(outputDir: File, zipFile: File): List<String> {
            zipFile.inputStream().use { zfis ->
                ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, zfis).use { zip ->
                    var entry: ZipArchiveEntry?
                    val list = arrayListOf<String>()
                    do {
                        entry = (zip as ZipArchiveInputStream).nextZipEntry
                        if (entry == null)
                            break
                        list.add(entry.name)
                        val file = File(outputDir, entry.name)
                        if (entry.isDirectory) {
                            file.mkdirs()
                        } else {
                            if (!file.parentFile.exists()) {
                                file.parentFile.mkdirs()
                            }
                            file.outputStream().use { output ->
                                zip.copyTo(output)
                            }
                        }
                    } while (true)
                    return list
                }
            }

        }
    }
}