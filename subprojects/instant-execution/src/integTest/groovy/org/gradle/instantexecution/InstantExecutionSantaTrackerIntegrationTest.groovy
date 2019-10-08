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

import spock.lang.Unroll


/**
 * Integration test Santa Tracker android app against AGP nightly.
 */
class InstantExecutionSantaTrackerIntegrationTest extends AbstractInstantExecutionAndroidIntegrationTest {

    def setup() {
        executer.beforeExecute {
            executer.noDeprecationChecks()
            executer.withRepositoryMirrors()
        }
    }

    @Unroll
    def "assembleDebug --dry-run on Santa Tracker #flavor (invokedFromIde: #invokedFromIde)"() {

        given:
        copyRemoteProject(remoteProject)
        withAgpNightly()

        when:
        instantRun ':santa-tracker:assembleDebug', '--dry-run', '--no-build-cache', "-Pandroid.injected.invoked.from.ide=$invokedFromIde"

        then:
        instantRun ':santa-tracker:assembleDebug', '--dry-run', '--no-build-cache', "-Pandroid.injected.invoked.from.ide=$invokedFromIde"

        where:
        invokedFromIde | flavor | remoteProject
        false          | 'Java' | "santaTrackerJava"
        true           | 'Java' | "santaTrackerJava"
        // 'Kotlin' | "santaTrackerKotlin" // TODO:instant-execution Instant execution state could not be cached.
    }

    @Unroll
    def "assembleDebug up-to-date on Santa Tracker Java (invokedFromIde: #invokedFromIde)"() {
        given:
        copyRemoteProject("santaTrackerJava")
        withAgpNightly()

        when:
        instantRun("assembleDebug", "--no-build-cache", "-Pandroid.injected.invoked.from.ide=$invokedFromIde")

        then:
        instantRun("assembleDebug", "--no-build-cache", "-Pandroid.injected.invoked.from.ide=$invokedFromIde")

        where:
        invokedFromIde << [true, false]
    }

    @Unroll
    def "supported tasks clean assembleDebug on Santa Tracker Java (invokedFromIde: #invokedFromIde)"() {

        given:
        copyRemoteProject("santaTrackerJava")
        withAgpNightly()

        when:
        executer.expectDeprecationWarning() // Coming from Android plugin
        instantRun("assembleDebug", "--no-build-cache", "-Pandroid.injected.invoked.from.ide=$invokedFromIde")

        and:
        executer.expectDeprecationWarning() // Coming from Android plugin
        run 'clean'

        then:
        // Instant execution avoid registering the listener inside Android plugin
        instantRun("assembleDebug", "--no-build-cache", "-Pandroid.injected.invoked.from.ide=$invokedFromIde")

        where:
        invokedFromIde << [true, false]
    }
}
