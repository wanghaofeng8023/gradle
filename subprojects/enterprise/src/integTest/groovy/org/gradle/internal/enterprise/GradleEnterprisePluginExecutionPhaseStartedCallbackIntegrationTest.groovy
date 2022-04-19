/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.enterprise

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class GradleEnterprisePluginExecutionPhaseStartedCallbackIntegrationTest extends AbstractIntegrationSpec {

    def plugin = new GradleEnterprisePluginCheckInFixture(testDirectory, mavenRepo, createExecuter())

    def setup() {
        settingsFile << plugin.pluginManagement() << plugin.plugins()
        plugin.publishDummyPlugin(executer)
    }

    def "receives execution phase started callback if build succeeds"() {
        given:
        buildFile << """
        tasks.register("success")
        """
        when:
        succeeds "success"

        then:
        plugin.invokedExecutionPhaseStartedCallbackOnce(output)

        when:
        succeeds "success"

        then:
        plugin.invokedExecutionPhaseStartedCallbackOnce(output)
    }


    def "receives execution phase started callback if build fails"() {
        given:
        buildFile << """
        tasks.register("failure") {
            doLast {
                throw new GradleException("Expected failure")
            }
        }
        """
        when:
        fails "failure"

        then:
        plugin.invokedExecutionPhaseStartedCallbackOnce(output)

        when:
        fails "failure"

        then:
        plugin.invokedExecutionPhaseStartedCallbackOnce(output)
    }

    def "does not receive execution phase started callback if configuration fails"() {
        given:
        buildFile << """
            tasks.register("t")
            throw new GradleException("Expected configuration failure")
        """
        when:
        fails "t"

        then:
        plugin.didNotInvokeExecutionPhaseStartedCallback(output)
    }

    def "receives execution phase started callback only once per build tree"() {
        given:
        def buildSrc = testDirectory.createDir("buildSrc")
        buildSrc.file("src/main/groovy/sneaky-buildsrc-plugin.gradle") << """
        println "Sneaky buildSrc plugin"
        """
        buildSrc.file("build.gradle") << applyPrecompiledGroovyPlugin()

        def includedPlugin = testDirectory.createDir("included-plugin")
        includedPlugin.file("build.gradle") << applyPrecompiledGroovyPlugin()
        includedPlugin.file("src/main/groovy/sneaky-included-plugin.gradle") << """
        println "Sneaky included plugin"
        """

        def includedProject = testDirectory.createDir("included-project")
        includedProject.file("build.gradle") << """
        tasks.register("includedTask")
        """

        settingsFile << """
        includeBuild("$includedPlugin.name")
        includeBuild("$includedProject.name")
        """

        buildFile << """
            plugins {
                id "sneaky-included-plugin"
                id "sneaky-buildsrc-plugin"
            }

            tasks.register("success") {
                dependsOn gradle.includedBuild('$includedProject.name').task(':includedTask')
            }
        """

        when:
        succeeds "success"

        then:
        plugin.invokedExecutionPhaseStartedCallbackOnce(output)

        when:
        succeeds "success"

        then:
        plugin.invokedExecutionPhaseStartedCallbackOnce(output)
    }

    private static String applyPrecompiledGroovyPlugin() {
        return """
        plugins {
            id 'groovy-gradle-plugin'
        }
        """
    }
}
