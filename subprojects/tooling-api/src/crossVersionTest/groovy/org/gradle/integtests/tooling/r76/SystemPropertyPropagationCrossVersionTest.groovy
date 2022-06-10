/*
 * Copyright 2022 the original author or authors.
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

package org.gradle.integtests.tooling.r76

import org.gradle.integtests.tooling.fixture.TargetGradleVersion
import org.gradle.integtests.tooling.fixture.ToolingApiSpecification
import org.gradle.integtests.tooling.fixture.ToolingApiVersion

class SystemPropertyPropagationCrossVersionTest extends ToolingApiSpecification {

    def setup() {
        System.setProperty('tapiClientSystemPropertyKey', 'should be available only in the TAPI client')
        buildFile << '''
            tasks.register('printSystemProperty') {
                doLast {
                    System.out.println(System.getProperty('tapiClientSystemPropertyKey', '<undefined>'))
                }
            }
        '''
    }

    @ToolingApiVersion(">=7.6")
    @TargetGradleVersion(">=2.6 <7.6")
    def "Custom system properties are ignored in older Gradle versions"() {
        when:
        withConnection {
            def launcher = newBuild().forTasks('printSystemProperty').withSystemProperties(['customKey' : 'customValue'])
            collectOutputs(launcher)
            launcher.run()
        }

        then:
        stdout.toString().contains('should be available only in the TAPI client')
    }

    @ToolingApiVersion('>=7.6')
    @TargetGradleVersion(">=7.6")
    def "Can control system properties propagated from the client to the target build"() {
        when:
        withConnection {
            def launcher = newBuild().forTasks('printSystemProperty').withSystemProperties(['customKey' : 'customValue'])
            collectOutputs(launcher)
            launcher.run()
        }

        then:
        stdout.toString().contains('<undefined>')
    }
}
