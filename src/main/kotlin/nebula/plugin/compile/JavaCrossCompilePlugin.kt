package nebula.plugin.compile

import nebula.plugin.compile.provider.DefaultLocationJDKPathProvider
import nebula.plugin.compile.provider.EnvironmentJDKPathProvider
import nebula.plugin.compile.provider.SDKManJDKPathProvider
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class JavaCrossCompilePlugin : Plugin<Project> {
    companion object {
        const val RT_JAR_PATH = "jre/lib/rt.jar"
        const val CLASSES_JAR_PATH = "../Classes/classes.jar"
        val ADDITIONAL_JARS = listOf("jsse", "jce", "charsets")

        val logger: Logger = LoggerFactory.getLogger(JavaCrossCompilePlugin::class.java)
        val providers = listOf(EnvironmentJDKPathProvider(), DefaultLocationJDKPathProvider(), SDKManJDKPathProvider())
    }

    override fun apply(project: Project) {
        project.plugins.apply(JavaBasePlugin::class.java)
        project.afterEvaluate {
            configureBootstrapClasspath(project)
        }
    }

    private fun configureBootstrapClasspath(project: Project) {
        val convention = project.convention.plugins["java"] as JavaPluginConvention? ?: return
        val targetCompatibility = convention.targetCompatibility
        if (targetCompatibility != JavaVersion.current()) {
            with(project.tasks) {
                val location by lazy { targetCompatibility.locate() }
                withType(JavaCompile::class.java) {
                    it.options.bootClasspath = location.bootClasspath
                }
                withType(GroovyCompile::class.java) {
                    it.options.bootClasspath = location.bootClasspath
                }
                project.plugins.withId("kotlin") {
                    withType(KotlinCompile::class.java) {
                        it.kotlinOptions.jdkHome = location.jdkHome
                    }
                }
            }
        }
    }

    private fun JavaVersion.locate(): JavaLocation {
        logger.debug("Locating JDK for $this")
        val jdkHome = providers
                .firstNotNullResult {
                    val jdkHome = it.provide(this)
                    if (jdkHome == null) {
                        logger.debug("Provider $it did not find a JDK")
                        null
                    } else {
                        logger.debug("Provider $it found a JDK at $jdkHome")
                        jdkHome
                    }
                } ?: throw cannotLocate()
        logger.debug("Found JDK for $this at $jdkHome")
        val runtimeJars = listOf(
                File(jdkHome, RT_JAR_PATH),
                File(jdkHome, CLASSES_JAR_PATH)
        )
        val runtimeJar = runtimeJars
                .firstNotNullResult {
                    if (it.exists()) {
                        logger.debug("Found runtime classes jar $it")
                        it
                    } else {
                        logger.debug("Runtime classes jar $it does not exist")
                        null
                    }
                } ?: throw cannotLocate()
        val libDir = runtimeJar.parentFile
        val jarFiles = listOf(runtimeJar) + ADDITIONAL_JARS.map { File(libDir, "$it.jar") }
        val classpath = jarFiles.joinToString(File.pathSeparator)
        return JavaLocation(jdkHome, classpath)
    }

    private fun JavaVersion.cannotLocate(): IllegalStateException = IllegalStateException("Could not locate a compatible JDK for target compatibility $this. Change the source/target compatibility, set a JDK_1$majorVersion environment variable with the location, or install to one of the default search locations")

    data class JavaLocation(val jdkHome: String, val bootClasspath: String)
}
