package nebula.plugin.compile

import com.netflix.nebula.interop.versionGreaterThan
import com.netflix.nebula.interop.versionLessThan
import nebula.plugin.compile.provider.DefaultLocationJDKPathProvider
import nebula.plugin.compile.provider.EnvironmentJDKPathProvider
import nebula.plugin.compile.provider.JDKPathProvider
import nebula.plugin.compile.provider.SDKManJDKPathProvider
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

class JavaCrossCompilePlugin @Inject constructor(private val providerFactory: ProviderFactory) : Plugin<Project> {
    companion object {
        const val RT_JAR_PATH = "jre/lib/rt.jar"
        const val CLASSES_JAR_PATH = "../Classes/classes.jar"
        val ADDITIONAL_JARS = listOf("jsse", "jce", "charsets")

        val logger: Logger = LoggerFactory.getLogger(JavaCrossCompilePlugin::class.java)
    }
    override fun apply(project: Project) {
        val providers = listOf(EnvironmentJDKPathProvider(providerFactory, project), DefaultLocationJDKPathProvider(), SDKManJDKPathProvider(providerFactory, project))
        val extension = project.extensions.create("javaCrossCompile", JavaCrossCompileExtension::class.java)
        project.plugins.apply(JavaBasePlugin::class.java)
        project.afterEvaluate {
            configureBootstrapClasspath(project, providers, extension)
        }
    }

    private fun configureBootstrapClasspath(project: Project, providers: List<JDKPathProvider>, extension: JavaCrossCompileExtension) {
        val convention = project.convention.plugins["java"] as JavaPluginConvention? ?: return
        val targetCompatibility = convention.targetCompatibility
        if (targetCompatibility < JavaVersion.current()) {
            with(project.tasks) {
                val location by lazy { targetCompatibility.locate(project, providers) }
                withType(JavaCompile::class.java) {
                    if (JavaVersion.current() >= JavaVersion.VERSION_1_9) {
                        if (project.gradle.versionLessThan("6.6-rc-1")) {
                            it.options.compilerArgs.addAll(listOf("--release", targetCompatibility.majorVersion))
                        } else {
                            it.options.release.set(targetCompatibility.majorVersion.toInt())
                        }
                    } else {
                        if (project.gradle.versionGreaterThan("4.2.1")) {
                            it.options.bootstrapClasspath = location.bootstrapClasspath
                        } else {
                            it.options.javaClass.getDeclaredMethod("setBootClasspath", String::class.java).invoke(it.options, location.bootClasspath)
                        }
                    }
                }
                //disable is useful when you have single jdk 11 on a machine and you target 8 in your build
                //can be removed when https://youtrack.jetbrains.com/issue/KT-29974 is resolved and we can use similar approach as for java
                if (! extension.disableKotlinSupport) {
                    project.plugins.withId("kotlin") {
                        withType(KotlinCompile::class.java) {
                            it.kotlinOptions.jdkHome = location.jdkHome
                        }
                    }
                }
            }
        }
    }

    private fun JavaVersion.locate(project: Project, providers: List<JDKPathProvider>): JavaLocation {
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
        return JavaLocation(jdkHome, project.files(classpath))
    }

    private fun JavaVersion.cannotLocate(): IllegalStateException = IllegalStateException("Could not locate a compatible JDK for target compatibility $this. Change the source/target compatibility, set a JDK_1$majorVersion environment variable with the location, or install to one of the default search locations")

    data class JavaLocation(val jdkHome: String, val bootstrapClasspath: FileCollection) {
        val bootClasspath: String
            get() = bootstrapClasspath.joinToString(File.pathSeparator)
    }
}
