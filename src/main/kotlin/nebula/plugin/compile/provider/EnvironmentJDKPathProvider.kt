package nebula.plugin.compile.provider

import com.netflix.nebula.interop.versionLessThan
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory

/**
 * Provide JDK path from environment variables.
 */
class EnvironmentJDKPathProvider constructor(private val providerFactory: ProviderFactory, private val project: Project) : JDKPathProvider {
    override fun provide(javaVersion: JavaVersion): String? {
        return if(project.gradle.versionLessThan("6.5")) {
            val jdkEnvVariable = providerFactory.environmentVariable("JDK_1${javaVersion.majorVersion}").forUseAtConfigurationTime()
            if(jdkEnvVariable.isPresent) jdkEnvVariable.get() else null
        } else {
            System.getenv("JDK_1${javaVersion.majorVersion}")
        }
    }
}
