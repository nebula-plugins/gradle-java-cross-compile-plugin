package nebula.plugin.compile

import nebula.test.IntegrationSpec
import org.gradle.api.JavaVersion
import org.junit.Assume
import spock.lang.Unroll

class JavaCrossCompilePluginIntegrationSpec extends IntegrationSpec {
    def 'plugin applies'() {
        buildFile << """\
            apply plugin: 'com.netflix.nebula.java-cross-compile'
        """

        when:
        runTasksSuccessfully('help')

        then:
        noExceptionThrown()
    }

    @Unroll
    def 'sourceCompatibility set to #sourceCompatibility'(Double sourceCompatibility) {
        buildFile << """\
            apply plugin: 'com.netflix.nebula.java-cross-compile'
            apply plugin: 'java'
            
            java {
                 sourceCompatibility = $sourceCompatibility
            }
        """

        when:
        def result = runTasks('help')

        then:
        println result.standardOutput
        println result.standardError
        result.rethrowFailure()

        where:
        sourceCompatibility | _
     //   1.7 | _
        1.8 | _
    }

    def 'missing jdk throws exception'() {
        Assume.assumeTrue(JavaVersion.current() < JavaVersion.VERSION_1_9)

        buildFile << """\
            apply plugin: 'com.netflix.nebula.java-cross-compile'
            apply plugin: 'java'
            
            java {
              sourceCompatibility = 1.4
            }
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
            apply plugin: 'com.netflix.nebula.java-cross-compile'
            apply plugin: 'java'
            
            java {
              sourceCompatibility = 1.7
            }
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
        '6.7'   | _
        '6.8'   | _
        '6.9'   | _
        '7.4'   | _
        //'current' | _
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

            apply plugin: 'com.netflix.nebula.java-cross-compile'
            apply plugin: 'kotlin'
            
            javaCrossCompile {
                disableKotlinSupport = true
            }
            
            java {
                sourceCompatibility = 1.8
            }
        """

        expect:
        def result = runTasksSuccessfully('help', '--warning-mode', 'none')
    }


    def 'Do not apply opinions if using Java Toolchains'() {
        buildFile << """\
            apply plugin: 'com.netflix.nebula.java-cross-compile'
            apply plugin: 'java'
            

            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(8)
                }
            }
           
        """

        writeHelloWorld('helloworld')

        when:
        def result = runTasks('compileJava', '-d')

        then:
        result.standardOutput.contains("Toolchain is configured for this project, skipping java-cross-compile plugin configuration")
    }
}
