package nebula.plugin.compile.provider

import com.netflix.nebula.interop.versionLessThan
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileFilter

/**
 * Provide JDK path from SDKMan candidates.
 */
class SDKManJDKPathProvider constructor(private val providerFactory: ProviderFactory, private val project: Project): JDKPathProvider {
    companion object {
        const val SDKMAN_JAVA_CANDIDATES = ".sdkman/candidates/java"

        val logger: Logger = LoggerFactory.getLogger(SDKManJDKPathProvider::class.java)
    }

    override fun provide(javaVersion: JavaVersion): String? {

        val userHome =  if(project.gradle.versionLessThan("6.5")) System.getProperty("user.home") else providerFactory.systemProperty("user.home").forUseAtConfigurationTime().get()
        val javaCandidates = File(userHome, SDKMAN_JAVA_CANDIDATES)
        val candidates = javaCandidates.listFiles(FileFilter { it.isDirectory })?.reversed() ?: emptyList()

        if (candidates.isEmpty()) {
            logger.debug("No candidates were found in $javaCandidates")
            return null
        }

        listOf("", "oracle", "zulu").forEach { variant ->
            val jdkHome = candidates.firstOrNull {
                logger.debug("Evaluating SDKMan candidate ${it.name}")
                isRightVersion(it.name, javaVersion) && (variant.isEmpty() || it.name.endsWith(variant))
            }
            if (jdkHome != null) {
                logger.debug("Found SDKMan provided JDK at $jdkHome")
                return jdkHome.absolutePath
            }
        }

        logger.debug("No JDKs found in candidate locations $candidates")
        return null
    }

    private fun isRightVersion(name: String, javaVersion: JavaVersion): Boolean =
            name.startsWith("${javaVersion.majorVersion}u") || name.startsWith("${javaVersion.majorVersion}.")
}
