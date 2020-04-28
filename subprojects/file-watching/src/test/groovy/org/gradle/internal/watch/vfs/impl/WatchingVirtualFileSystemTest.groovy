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

package org.gradle.internal.watch.vfs.impl

import org.gradle.internal.snapshot.AtomicSnapshotHierarchyReference
import org.gradle.internal.snapshot.CaseSensitivity
import org.gradle.internal.snapshot.SnapshotHierarchy
import org.gradle.internal.vfs.impl.AbstractVirtualFileSystem
import org.gradle.internal.vfs.impl.DefaultSnapshotHierarchy
import org.gradle.internal.watch.registry.FileWatcherRegistry
import org.gradle.internal.watch.registry.FileWatcherRegistryFactory
import spock.lang.Specification

class WatchingVirtualFileSystemTest extends Specification {
    def delegate = Mock(AbstractVirtualFileSystem)
    def watcherRegistryFactory = Mock(FileWatcherRegistryFactory)
    def watcherRegistry = Mock(FileWatcherRegistry)
    def capturingUpdateFunctionDecorator = Mock(DelegatingDiffCapturingUpdateFunctionDecorator)
    def rootHierarchy = Mock(SnapshotHierarchy)
    def rootReference = new AtomicSnapshotHierarchyReference(rootHierarchy)
    def watchingVirtualFileSystem = new WatchingVirtualFileSystem(watcherRegistryFactory, delegate, capturingUpdateFunctionDecorator, { -> true })
    def snapshotHierarchy = DefaultSnapshotHierarchy.empty(CaseSensitivity.CASE_SENSITIVE)

    def "invalidates the virtual file system before and after the build when watching is disabled"() {
        when:
        watchingVirtualFileSystem.afterBuildStarted(false)
        then:
        1 * delegate.root >> rootReference
        1 * rootHierarchy.empty()
        0 * _

        when:
        watchingVirtualFileSystem.beforeBuildFinished(false)
        then:
        1 * delegate.invalidateAll()
        0 * _
    }

    def "stops the watchers before the build when watching is disabled"() {
        when:
        watchingVirtualFileSystem.afterBuildStarted(true)
        then:
        _ * delegate.getRoot() >> new AtomicSnapshotHierarchyReference(snapshotHierarchy)
        1 * watcherRegistryFactory.createFileWatcherRegistry(_) >> watcherRegistry
        1 * capturingUpdateFunctionDecorator.setSnapshotDiffListener(_, _)
        0 * _

        when:
        watchingVirtualFileSystem.beforeBuildFinished(true)
        then:
        _ * delegate.getRoot() >> new AtomicSnapshotHierarchyReference(snapshotHierarchy)
        1 * watcherRegistry.getAndResetStatistics() >> Stub(FileWatcherRegistry.FileWatchingStatistics)
        0 * _

        when:
        watchingVirtualFileSystem.afterBuildStarted(false)
        then:
        1 * delegate.root >> rootReference
        1 * rootHierarchy.empty()
        1 * watcherRegistry.close()
        1 * capturingUpdateFunctionDecorator.stopListening()
        0 * _
    }

    def "retains the virtual file system when watching is enabled"() {
        when:
        watchingVirtualFileSystem.afterBuildStarted(true)
        then:
        _ * delegate.getRoot() >> new AtomicSnapshotHierarchyReference(snapshotHierarchy)
        1 * watcherRegistryFactory.createFileWatcherRegistry(_) >> watcherRegistry
        1 * capturingUpdateFunctionDecorator.setSnapshotDiffListener(_, _)
        0 * _

        when:
        watchingVirtualFileSystem.beforeBuildFinished(true)
        then:
        _ * delegate.getRoot() >> new AtomicSnapshotHierarchyReference(snapshotHierarchy)
        1 * watcherRegistry.getAndResetStatistics() >> Stub(FileWatcherRegistry.FileWatchingStatistics)
        0 * _

        when:
        watchingVirtualFileSystem.afterBuildStarted(true)
        then:
        _ * delegate.getRoot() >> new AtomicSnapshotHierarchyReference(snapshotHierarchy)
        1 * watcherRegistry.getAndResetStatistics() >> Stub(FileWatcherRegistry.FileWatchingStatistics)
        0 * _
    }
}
