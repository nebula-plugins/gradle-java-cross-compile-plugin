package nebula.plugin.compile

import nebula.plugin.compile.provider.DefaultLocationJDKPathProvider
import nebula.plugin.compile.provider.EnvironmentJDKPathProvider
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

class JavaCrossCompilePlugin : Plugin<Project> {
    companion object {
        const val RT_JAR_PATH = "jre/lib/rt.jar"
        val providers = listOf(EnvironmentJDKPathProvider(), DefaultLocationJDKPathProvider())
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
                withType(JavaCompile::class.java) {
                    it.options.bootClasspath = targetCompatibility.runtimeJar()
                }
                withType(GroovyCompile::class.java) {
                    it.options.bootClasspath = targetCompatibility.runtimeJar()
                }
                project.plugins.withId("kotlin") {
                    withType(KotlinCompile::class.java) {
                        it.kotlinOptions.jdkHome = targetCompatibility.jdkHome()
                    }
                }
            }
        }
    }

    private fun JavaVersion.jdkHome(): String {
        return providers
                .map { it.provide(this) }
                .filter { File(it, RT_JAR_PATH).exists() }
                .firstOrNull() ?: throw IllegalStateException("Could not locate a compatible JDK for target compatibility $this. Change the source/target compatibility, set a JDK_1$majorVersion environment variable with the location, or install to one of the default search locations")
    }

    private fun JavaVersion.runtimeJar(): String = File(jdkHome(), RT_JAR_PATH).absolutePath
}
