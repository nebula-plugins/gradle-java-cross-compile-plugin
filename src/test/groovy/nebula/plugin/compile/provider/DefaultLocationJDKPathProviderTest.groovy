/**
 *
 *  Copyright 2019 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package nebula.plugin.compile.provider

import nebula.test.ProjectSpec
import org.gradle.api.JavaVersion
import spock.lang.Subject
import spock.lang.Unroll

@Subject(DefaultLocationJDKPathProvider)
class DefaultLocationJDKPathProviderTest extends ProjectSpec {
    List<File> updatedBasePaths = new ArrayList()
    ObjectGraphBuilder builder

    def setup() {
        // setup to update read-only property
        builder = new ObjectGraphBuilder()
        builder.classLoader = this.class.classLoader
        builder.classNameResolver = "nebula.plugin.compile.provider"
    }

    def 'provides JDK from Ubuntu style location'() {
        given:
        File sampleUbuntuJDKPath = new File(project.projectDir, "java-8-openjdk")
        sampleUbuntuJDKPath.mkdirs()
        updatedBasePaths.add(project.projectDir)

        def provider = builder.defaultLocationJDKPathProvider(basePaths: updatedBasePaths) // update read-only property

        when:
        def actualJdkPath = provider.provide(JavaVersion.VERSION_1_8)
        def expectedPath = sampleUbuntuJDKPath.path

        then:
        assert actualJdkPath == expectedPath
    }

    def 'provides JDK from OSX style location'() {
        given:
        File sampleOSXJDKPath = new File(project.projectDir, "zulu1.8.0_181.jdk/Contents/Home")
        sampleOSXJDKPath.mkdirs()
        updatedBasePaths.add(project.projectDir)

        def provider = builder.defaultLocationJDKPathProvider(basePaths: updatedBasePaths) // update read-only property

        when:
        def actualJdkPath = provider.provide(JavaVersion.VERSION_1_8)
        def expectedPath = sampleOSXJDKPath.path

        then:
        assert actualJdkPath == expectedPath
    }

    def 'provides JDK from Windows style location'() {
        given:
        File sampleWindowsJDKPath = new File(project.projectDir, "jdk1.8.0_172")
        sampleWindowsJDKPath.mkdirs()
        updatedBasePaths.add(project.projectDir)

        def provider = builder.defaultLocationJDKPathProvider(basePaths: updatedBasePaths) // update read-only property

        when:
        def actualJdkPath = provider.provide(JavaVersion.VERSION_1_8)
        def expectedPath = sampleWindowsJDKPath.path

        then:
        assert actualJdkPath == expectedPath
    }

    @Unroll
    def 'provides JDK from Github Actions style location for #javaPackage/#javaVersion/#architecture'() {
        given:
        File sampleGithubActionsJDKPath = new File(project.projectDir, "$javaPackage/$javaVersion/$architecture")
        sampleGithubActionsJDKPath.mkdirs()
        updatedBasePaths.add(project.projectDir)

        def provider = builder.defaultLocationJDKPathProvider(basePaths: updatedBasePaths) // update read-only property

        when:
        def actualJdkPath = provider.provide(JavaVersion.VERSION_1_8)
        def expectedPath = sampleGithubActionsJDKPath.path

        then:
        assert actualJdkPath == expectedPath

        where:
        javaVersion | javaPackage | architecture
        '8.0.232'   | 'jdk'       | 'x64'
        '8.0.172'   | 'jre'       | 'x86'
    }

    def 'does not find JDK when none is set'() {
        given:
        def provider = builder.defaultLocationJDKPathProvider(basePaths: updatedBasePaths) // update read-only property

        when:
        def actualJdkPath = provider.provide(JavaVersion.VERSION_1_8)

        then:
        assert actualJdkPath == null
    }

}
