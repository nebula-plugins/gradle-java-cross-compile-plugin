package nebula.plugin.compile.provider

import org.gradle.api.JavaVersion

interface JDKPathProvider {
    fun provide(javaVersion: JavaVersion): String?
}