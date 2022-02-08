package nebula.plugin.compile

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun configureKotlinCompiler(project: Project, location: JavaCrossCompilePlugin.JavaLocation) {
    project.tasks.withType(KotlinCompile::class.java).configureEach {
        it.kotlinOptions.jdkHome = location.jdkHome
    }
}