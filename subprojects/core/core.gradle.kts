/*
 * Copyright 2010 the original author or authors.
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
import org.gradle.gradlebuild.testing.integrationtests.cleanup.WhenNotEmpty

plugins {
    gradlebuild.distribution.`core-api-java`
}

configurations {
    register("reports")
}

tasks.classpathManifest {
    optionalProjects.add(":kotlinDsl")
}

dependencies {
    implementation(project(":baseServices"))
    implementation(project(":baseServicesGroovy"))
    implementation(project(":messaging"))
    implementation(project(":logging"))
    implementation(project(":resources"))
    implementation(project(":cli"))
    implementation(project(":buildOption"))
    implementation(project(":native"))
    implementation(project(":modelCore"))
    implementation(project(":persistentCache"))
    implementation(project(":buildCache"))
    implementation(project(":buildCachePackaging"))
    implementation(project(":coreApi"))
    implementation(project(":files"))
    implementation(project(":fileCollections"))
    implementation(project(":processServices"))
    implementation(project(":jvmServices"))
    implementation(project(":modelGroovy"))
    implementation(project(":snapshots"))
    implementation(project(":fileWatching"))
    implementation(project(":execution"))
    implementation(project(":workerProcesses"))
    implementation(project(":normalizationJava"))

    implementation(library("groovy"))
    implementation(library("ant"))
    implementation(library("guava"))
    implementation(library("inject"))
    implementation(library("asm"))
    implementation(library("asm_commons"))
    implementation(library("slf4j_api"))
    implementation(library("commons_io"))
    implementation(library("commons_lang"))
    implementation(library("nativePlatform"))
    implementation(library("commons_compress"))
    implementation(library("xmlApis"))

    runtimeOnly(project(":docs"))

    testImplementation(project(":plugins"))
    testImplementation(project(":testingBase"))
    testImplementation(project(":platformNative"))
    testImplementation(testLibrary("jsoup"))
    testImplementation(library("log4j_to_slf4j"))
    testImplementation(library("jcl_to_slf4j"))

    testRuntimeOnly(library("xerces"))
    testRuntimeOnly(project(":diagnostics"))
    testRuntimeOnly(project(":compositeBuilds"))

    testFixturesApi(project(":baseServices")) {
        because("test fixtures expose Action")
    }
    testFixturesApi(project(":baseServicesGroovy")) {
        because("test fixtures expose AndSpec")
    }
    testFixturesApi(project(":coreApi")) {
        because("test fixtures expose Task")
    }
    testFixturesApi(project(":logging")) {
        because("test fixtures expose Logger")
    }
    testFixturesApi(project(":modelCore")) {
        because("test fixtures expose IConventionAware")
    }
    testFixturesApi(project(":buildCache")) {
        because("test fixtures expose BuildCacheController")
    }
    testFixturesApi(project(":execution")) {
        because("test fixtures expose OutputChangeListener")
    }
    testFixturesApi(project(":native")) {
        because("test fixtures expose FileSystem")
    }
    testFixturesImplementation(project(":fileCollections"))
    testFixturesImplementation(project(":native"))
    testFixturesImplementation(project(":resources"))
    testFixturesImplementation(project(":processServices"))
    testFixturesImplementation(project(":internalTesting"))
    testFixturesImplementation(project(":messaging"))
    testFixturesImplementation(project(":persistentCache"))
    testFixturesImplementation(project(":snapshots"))
    testFixturesImplementation(library("ivy"))
    testFixturesImplementation(library("slf4j_api"))
    testFixturesImplementation(library("guava"))
    testFixturesImplementation(library("ant"))

    testImplementation(project(":dependencyManagement"))

    testImplementation(testFixtures(project(":coreApi")))
    testImplementation(testFixtures(project(":messaging")))
    testImplementation(testFixtures(project(":modelCore")))
    testImplementation(testFixtures(project(":logging")))
    testImplementation(testFixtures(project(":baseServices")))
    testImplementation(testFixtures(project(":diagnostics")))

    testRuntimeOnly(project(":runtimeApiInfo"))
    testRuntimeOnly(project(":kotlinDsl"))
    testRuntimeOnly(project(":kotlinDslProviderPlugins"))

    integTestImplementation(project(":workers"))
    integTestImplementation(project(":dependencyManagement"))
    integTestImplementation(project(":launcher"))
    integTestImplementation(project(":plugins"))
    integTestImplementation(library("jansi"))
    integTestImplementation(library("jetbrains_annotations"))
    integTestImplementation(testLibrary("jetty"))
    integTestImplementation(testLibrary("littleproxy"))
    integTestImplementation(testFixtures(project(":native")))

    integTestRuntimeOnly(project(":testingJunitPlatform"))
    integTestRuntimeOnly(project(":maven"))
    integTestRuntimeOnly(project(":apiMetadata"))
    integTestRuntimeOnly(project(":kotlinDsl"))
    integTestRuntimeOnly(project(":kotlinDslProviderPlugins"))
    integTestRuntimeOnly(project(":kotlinDslToolingBuilders"))
    integTestRuntimeOnly(project(":testingJunitPlatform"))
    integTestRuntimeOnly(project(":testKit"))

    crossVersionTestRuntimeOnly(project(":testingJunitPlatform"))
}

strictCompile {
    ignoreRawTypes() // raw types used in public API
    ignoreParameterizedVarargType() // TODO remove this and address warnings and/or add the RIGHT ignores here
}

classycle {
    excludePatterns.set(listOf("org/gradle/**"))
}

tasks.test {
    setForkEvery(200)
}

val generatedResourcesDir = gradlebuildJava.generatedResourcesDir

listOf(tasks.compileGroovy, tasks.compileTestGroovy).forEach {
    it { groovyOptions.fork("memoryInitialSize" to "128M", "memoryMaximumSize" to "1G") }
}

val pluginsManifest by tasks.registering(WriteProperties::class) {
    property("plugins", provider {
        rootProject.subprojects.filter {
            it.plugins.hasPlugin(gradlebuild.distribution.PluginsPlugin::class)
                && it.plugins.hasPlugin(gradlebuild.distribution.ApiPlugin::class)
        }.map { it.base.archivesBaseName }.sorted().joinToString(",")
    })
    outputFile = File(generatedResourcesDir, "gradle-plugins.properties")
}

sourceSets.main {
    output.dir(generatedResourcesDir, "builtBy" to pluginsManifest)
}

val implementationPluginsManifest by tasks.registering(WriteProperties::class) {
    property("plugins", provider {
        rootProject.subprojects.filter {
            it.plugins.hasPlugin(gradlebuild.distribution.PluginsPlugin::class)
                && it.plugins.hasPlugin(gradlebuild.distribution.ImplementationPlugin::class)
        }.map { it.base.archivesBaseName }.sorted().joinToString(",")
    })
    outputFile = File(generatedResourcesDir, "gradle-implementation-plugins.properties")
}

sourceSets.main {
    output.dir(generatedResourcesDir, "builtBy" to implementationPluginsManifest)
}

testFilesCleanup {
    policy.set(WhenNotEmpty.REPORT)
}
