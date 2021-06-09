package nebula.plugin.compile

import nebula.test.IntegrationSpec
import org.gradle.api.JavaVersion
import org.junit.Assume
import spock.lang.Unroll

class JavaCrossCompilePluginIntegrationSpec extends IntegrationSpec {
    def 'plugin applies'() {
        buildFile << """\
            apply plugin: 'nebula.java-cross-compile'
        """

        when:
        runTasksSuccessfully('help')

        then:
        noExceptionThrown()
    }

    @Unroll
    def 'sourceCompatibility set to #sourceCompatibility'(Double sourceCompatibility) {
        buildFile << """\
            apply plugin: 'nebula.java-cross-compile'
            apply plugin: 'java'
            
            sourceCompatibility = $sourceCompatibility
        """

        when:
        def result = runTasks('help')

        then:
        println result.standardOutput
        println result.standardError
        result.rethrowFailure()

        where:
        sourceCompatibility | _
        1.7 | _
        1.8 | _
    }

    def 'missing jdk throws exception'() {
        Assume.assumeTrue(JavaVersion.current() < JavaVersion.VERSION_1_9)

        buildFile << """\
            apply plugin: 'nebula.java-cross-compile'
            apply plugin: 'java'
            
            sourceCompatibility = 1.4
        """

        when:
        def result = runTasks('help')

        then:
        def failure = result.failure
        failure != null
        failure.cause.cause.message == 'Could not locate a compatible JDK for target compatibility 1.4. Change the source/target compatibility, set a JDK_14 environment variable with the location, or install to one of the default search locations'
    }

    @Unroll
    def 'java compilation does not warn about bootstrap class path (gradle #gradle)'() {
        buildFile << """\
            apply plugin: 'nebula.java-cross-compile'
            apply plugin: 'java'
            
            sourceCompatibility = 1.7
        """
        if (gradle != 'current') {
            gradleVersion = gradle
        }

        writeHelloWorld('helloworld')

        when:
        def result = runTasks('compileJava')

        then:
        !result.standardError.contains("warning: [options] bootstrap class path not set in conjunction with -source 1.7")

        where:
        gradle    | _
        '4.2.1'   | _
        'current' | _
    }

    def 'kotlin cross compile can be disabled'() {
        Assume.assumeTrue(JavaVersion.current() >= JavaVersion.VERSION_1_9)
        buildFile << """\
            buildscript {
                repositories {
                    mavenCentral()
                }
            
                dependencies {
                    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50"
                }
            }

            apply plugin: 'nebula.java-cross-compile'
            apply plugin: 'kotlin'
            
            javaCrossCompile {
                disableKotlinSupport = true
            }
            
            sourceCompatibility = 1.8
        """

        expect:
        def result = runTasksSuccessfully('help')
    }
}
