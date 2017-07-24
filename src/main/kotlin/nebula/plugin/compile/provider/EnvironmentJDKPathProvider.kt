package nebula.plugin.compile.provider

import org.gradle.api.JavaVersion

/**
 * Provide JDK path from environment variables.
 */
class EnvironmentJDKPathProvider : JDKPathProvider {
    override fun provide(javaVersion: JavaVersion): String? {
        return System.getenv("JDK_1${javaVersion.majorVersion}")
    }
}
