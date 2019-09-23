package nebula.plugin.compile.provider

import org.gradle.api.JavaVersion
import org.gradle.api.Project

/**
 * Provide JDK path from properties variables.
 */
class PropertiesJDKPathProvider(val project: Project) : JDKPathProvider {
    companion object Factory: JDKPathProviderProjectAware,JDKPathProvider {
        override fun provide(javaVersion: JavaVersion): String? {
            return null;
        }

        override fun getJDKPathProvider(project: Project): JDKPathProvider {
        return PropertiesJDKPathProvider(project)
      }
    }

    override fun provide(javaVersion: JavaVersion): String? {
        val key = "JDK_1${javaVersion.majorVersion}"
        if(project.hasProperty(key)) {
            return project.property(key).toString()
        } else {
            return null
        }
    }
}
