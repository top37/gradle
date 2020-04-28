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
plugins {
    gradlebuild.internal.kotlin
}

dependencies {
    testFixturesImplementation(project(":baseServices"))
    testFixturesImplementation(project(":core"))
    testFixturesImplementation(project(":internalTesting"))
    testFixturesImplementation(project(":internalIntegTesting"))

    testImplementation(project(":kotlinDslTestFixtures"))
    testImplementation(testFixtures(project(":core")))

    integTestImplementation(project(":logging"))
    integTestImplementation(project(":persistentCache"))
    integTestImplementation(project(":launcher"))
    integTestImplementation(project(":fileWatching"))
    integTestImplementation(library("slf4j_api"))
    integTestImplementation(testLibrary("jetty"))

    integTestRuntimeOnly(project(":runtimeApiInfo"))
}

tasks.integTest {
    options {
        require(this is JUnitOptions)
        excludeCategories("org.gradle.soak.categories.SoakTest")
    }
}

tasks.register("soakIntegTest", org.gradle.gradlebuild.test.integrationtests.SoakTest::class) {
    val integTestSourceSet = sourceSets.integTest.get()
    testClassesDirs = integTestSourceSet.output.classesDirs
    classpath = integTestSourceSet.runtimeClasspath
    systemProperty("org.gradle.soaktest", "true")
    options {
        require(this is JUnitOptions)
        includeCategories("org.gradle.soak.categories.SoakTest")
    }
}

classycle {
    excludePatterns.set(listOf("META-INF/*.kotlin_module"))
}
