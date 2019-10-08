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

package org.gradle.instantexecution

import org.gradle.integtests.fixtures.TestResources
import org.junit.Rule
import spock.lang.Unroll


class InstantExecutionAndroidIntegrationTest extends AbstractInstantExecutionAndroidIntegrationTest {

    @Rule
    TestResources resources = new TestResources(temporaryFolder, "builds")

    def instantExecution

    def setup() {
        executer.noDeprecationChecks()
        executer.withRepositoryMirrors()

        def rootDir = file("android-3.6-mini")
        executer.beforeExecute {
            inDirectory(rootDir)
        }
        withAgpNightly(rootDir.file("build.gradle"))

        instantExecution = newInstantExecutionFixture()
    }

    @Unroll
    def "android 3.6 minimal build assembleDebug --dry-run (invokedFromIde: #invokedFromIde)"() {

        when:
        instantRun("assembleDebug", "--dry-run", "-Pandroid.injected.invoked.from.ide=$invokedFromIde")

        then:
        instantExecution.assertStateStored()

        when:
        instantRun("assembleDebug", "--dry-run", "-Pandroid.injected.invoked.from.ide=$invokedFromIde")

        then:
        instantExecution.assertStateLoaded()

        where:
        invokedFromIde << [true, false]
    }

    @Unroll
    def "android 3.6 minimal build assembleDebug up-to-date (invokedFromIde: #invokedFromIde)"() {
        when:
        instantRun("assembleDebug", "-Pandroid.injected.invoked.from.ide=$invokedFromIde")

        then:
        instantExecution.assertStateStored()

        when:
        instantRun("assembleDebug", "-Pandroid.injected.invoked.from.ide=$invokedFromIde")

        then:
        instantExecution.assertStateLoaded()

        where:
        invokedFromIde << [true, false]
    }

    @Unroll
    def "android 3.6 minimal build clean assembleDebug (invokedFromIde: #invokedFromIde)"() {
        when:
        executer.expectDeprecationWarning() // Coming from Android plugin
        instantRun("assembleDebug", "-Pandroid.injected.invoked.from.ide=$invokedFromIde")

        then:
        instantExecution.assertStateStored()

        when:
        executer.expectDeprecationWarning() // Coming from Android plugin
        run 'clean'
        // Instant execution avoid registering the listener inside Android plugin
        instantRun("assembleDebug", "-Pandroid.injected.invoked.from.ide=$invokedFromIde")

        then:
        instantExecution.assertStateLoaded()

        where:
        invokedFromIde << [true, false]
    }
}
