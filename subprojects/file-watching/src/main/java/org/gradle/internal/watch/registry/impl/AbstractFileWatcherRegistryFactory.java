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

package org.gradle.internal.watch.registry.impl;

import net.rubygrapefruit.platform.file.FileWatchEvent;
import net.rubygrapefruit.platform.file.FileWatcher;
import org.gradle.internal.watch.registry.FileWatcherRegistry;
import org.gradle.internal.watch.registry.FileWatcherRegistryFactory;
import org.gradle.internal.watch.registry.FileWatcherUpdater;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class AbstractFileWatcherRegistryFactory implements FileWatcherRegistryFactory {
    private static final int FILE_EVENT_QUEUE_SIZE = 4096;

    @Override
    public FileWatcherRegistry createFileWatcherRegistry(FileWatcherRegistry.ChangeHandler handler) {
        BlockingQueue<FileWatchEvent> fileEvents = new ArrayBlockingQueue<>(FILE_EVENT_QUEUE_SIZE);
        try {
            FileWatcher watcher = createFileWatcher(fileEvents);
            FileWatcherUpdater fileWatcherUpdater = createFileWatcherUpdater(watcher);
            return new DefaultFileWatcherRegistry(
                watcher,
                handler,
                fileWatcherUpdater,
                fileEvents
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract FileWatcher createFileWatcher(BlockingQueue<FileWatchEvent> fileEvents) throws InterruptedException;

    protected abstract FileWatcherUpdater createFileWatcherUpdater(FileWatcher watcher);
}
