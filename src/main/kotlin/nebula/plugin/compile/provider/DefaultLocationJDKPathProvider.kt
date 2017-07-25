package nebula.plugin.compile.provider

import org.gradle.api.JavaVersion
import java.io.File
import java.io.FileFilter

/**
 * Provide JDK path from known default installation locations.
 */
class DefaultLocationJDKPathProvider : JDKPathProvider {
    companion object {
        val basePaths = listOf(
                File("/usr/lib/jvm"),
                File("/Library/Java/JavaVirtualMachines"),
                File("""C:\Program Files\Java""")
        )
    }

    override fun provide(javaVersion: JavaVersion): String? {
        val candidates = basePaths.flatMap {
            it.listFiles(FileFilter { it.isDirectory })?.toList() ?: emptyList()
        }.reversed()

        if (candidates.isEmpty()) {
            return null
        }

        listOf("oracle", "openjdk").forEach { variant ->
            val jdkHome = candidates.firstOrNull {
                it.name.startsWith("java-${javaVersion.majorVersion}-$variant")
            }
            if (jdkHome != null) {
                return jdkHome.absolutePath
            }
        }

        val jdkHome = candidates.firstOrNull {
            val version = "1.${javaVersion.majorVersion}.0"
            it.name.startsWith("jdk1.$version.0") || it.name.startsWith("$version.jdk")
        }

        if (jdkHome != null) {
            val macOsJdkHome = File(jdkHome, "Contents/Home")
            if (macOsJdkHome.exists()) {
                return macOsJdkHome.absolutePath
            }
        }

        return jdkHome?.absolutePath
    }
}
