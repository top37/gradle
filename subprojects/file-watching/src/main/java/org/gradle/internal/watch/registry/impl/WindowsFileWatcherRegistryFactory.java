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

import net.rubygrapefruit.platform.Native;
import net.rubygrapefruit.platform.file.FileWatchEvent;
import net.rubygrapefruit.platform.file.FileWatcher;
import net.rubygrapefruit.platform.internal.jni.WindowsFileEventFunctions;
import org.gradle.internal.watch.registry.FileWatcherUpdater;

import java.util.concurrent.BlockingQueue;

public class WindowsFileWatcherRegistryFactory extends AbstractFileWatcherRegistryFactory {
    private static final int BUFFER_SIZE = 128 * 1024;

    @Override
    protected FileWatcher createFileWatcher(BlockingQueue<FileWatchEvent> fileEvents) throws InterruptedException {
        return Native.get(WindowsFileEventFunctions.class)
            .newWatcher(fileEvents)
            .withBufferSize(BUFFER_SIZE)
            .start();
    }

    @Override
    protected FileWatcherUpdater createFileWatcherUpdater(FileWatcher watcher) {
        return new HierarchicalFileWatcherUpdater(watcher);
    }
}
