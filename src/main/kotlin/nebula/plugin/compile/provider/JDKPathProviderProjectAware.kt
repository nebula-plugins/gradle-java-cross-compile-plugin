package nebula.plugin.compile.provider

import org.gradle.api.Project

interface JDKPathProviderProjectAware {
    fun getJDKPathProvider(project: Project): JDKPathProvider
}
