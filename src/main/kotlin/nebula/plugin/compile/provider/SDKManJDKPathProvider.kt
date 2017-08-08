package nebula.plugin.compile.provider

import org.gradle.api.JavaVersion
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileFilter

/**
 * Provide JDK path from SDKMan candidates.
 */
class SDKManJDKPathProvider : JDKPathProvider {
    companion object {
        const val SDKMAN_JAVA_CANDIDATES = ".sdkman/candidates/java"

        val logger: Logger = LoggerFactory.getLogger(SDKManJDKPathProvider::class.java)
    }

    override fun provide(javaVersion: JavaVersion): String? {
        val javaCandidates = File(System.getProperty("user.home"), SDKMAN_JAVA_CANDIDATES)
        val candidates = javaCandidates.listFiles(FileFilter { it.isDirectory }).reversed()

        if (candidates.isEmpty()) {
            logger.debug("No candidates were found in $javaCandidates")
            return null
        }

        listOf("", "oracle", "zulu").forEach { variant ->
            val jdkHome = candidates.firstOrNull {
                logger.debug("Evaluating SDKMan candidate ${it.name}")
                it.name.startsWith("${javaVersion.majorVersion}u") && (variant.isEmpty() || it.name.endsWith(variant))
            }
            if (jdkHome != null) {
                logger.debug("Found SDKMan provided JDK at $jdkHome")
                return jdkHome.absolutePath
            }
        }

        logger.debug("No JDKs found in candidate locations $candidates")
        return null
    }
}
