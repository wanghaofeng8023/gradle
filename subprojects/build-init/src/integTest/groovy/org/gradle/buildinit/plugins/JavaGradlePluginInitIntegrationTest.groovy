/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.buildinit.plugins

import org.gradle.buildinit.plugins.fixtures.ScriptDslFixture
import org.gradle.util.Requires
import org.gradle.util.TestPrecondition
import spock.lang.Unroll

class JavaGradlePluginInitIntegrationTest extends AbstractInitIntegrationSpec {

    @Override
    String subprojectName() { 'plugin' }

    // This test runs a build that itself runs a build in a test worker with 'gradleApi()' dependency, which needs to pick up Gradle modules from a real distribution
    @Requires(TestPrecondition.INSTALLED_DISTRIBUTION)
    @Unroll
    def "creates sample source if no source present with #scriptDsl build scripts"() {
        when:
        run('init', '--type', 'java-gradle-plugin', '--dsl', scriptDsl.id)

        then:
        subprojectDir.file("src/main/java").assertHasDescendants("some/thing/SomeThingPlugin.java")
        subprojectDir.file("src/test/java").assertHasDescendants("some/thing/SomeThingPluginTest.java")
        subprojectDir.file("src/functionalTest/java").assertHasDescendants("some/thing/SomeThingPluginFunctionalTest.java")

        and:
        commonJvmFilesGenerated(scriptDsl)

        when:
        run("build")

        then:
        assertTestPassed("some.thing.SomeThingPluginTest", "pluginRegistersATask")
        assertFunctionalTestPassed("some.thing.SomeThingPluginFunctionalTest", "canRunTask")

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }
}
