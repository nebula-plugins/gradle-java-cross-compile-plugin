package nebula.plugin.compile.provider

import org.gradle.api.JavaVersion
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileFilter

/**
 * Provide JDK path from known default installation locations.
 */
class DefaultLocationJDKPathProvider : JDKPathProvider {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(DefaultLocationJDKPathProvider::class.java)
        val basePaths = listOf(
                File("/usr/lib/jvm"),
                File("/Library/Java/JavaVirtualMachines"),
                File("""C:\Program Files\Java""")
        )
    }

    override fun provide(javaVersion: JavaVersion): String? {
        val candidates = basePaths.flatMap {
            it.listFiles(FileFilter { it.isDirectory })?.toList() ?: emptyList()
        }.reversed()

        if (candidates.isEmpty()) {
            logger.debug("No candidates were found in search locations $basePaths")
            return null
        }

        listOf("oracle", "openjdk").forEach { variant ->
            val jdkHome = candidates.firstOrNull {
                logger.debug("Evaluating Ubuntu candidate ${it.name}")
                it.name.startsWith("java-${javaVersion.majorVersion}-$variant")
            }
            if (jdkHome != null) {
                logger.debug("Found Ubuntu JDK at $jdkHome")
                return jdkHome.absolutePath
            }
        }

        val jdkHome = candidates.firstOrNull {
            logger.debug("Evaluating macOS/Windows candidate ${it.name}")
            val version = "1.${javaVersion.majorVersion}.0"
            val name = it.name
            listOf(
                    "jdk$version",
                    "jdk-$version",
                    "$version.jdk",
                    "zulu$version",
                    "zulu-${javaVersion.majorVersion}.jdk"
            ).any { name.startsWith(it) }
        }

        if (jdkHome != null) {
            val macOsJdkHome = File(jdkHome, "Contents/Home")
            if (macOsJdkHome.exists()) {
                logger.debug("Found macOS JDK at $jdkHome")
                return macOsJdkHome.absolutePath
            }
        }

        if (jdkHome == null) {
            logger.debug("No JDKs found in candidate locations $candidates ")
            return null
        }
        return jdkHome.absolutePath
    }
}
