package nebula.plugin.compile

import nebula.test.IntegrationTestKitSpec

class JavaCrossCompilePluginConfigurationCacheIntegrationSpec extends IntegrationTestKitSpec {

    def 'Uses configuration cache'() {
        buildFile << """\
            plugins {
                id 'nebula.java-cross-compile'
                id 'java'
            }

            sourceCompatibility = 1.8
        """
        writeHelloWorld('helloworld')

        when:
        runTasks('--configuration-cache', 'compileJava')
        def result = runTasks('--configuration-cache', 'compileJava', '-s')

        then:
        result.output.contains('Reusing configuration cache')
    }

}
