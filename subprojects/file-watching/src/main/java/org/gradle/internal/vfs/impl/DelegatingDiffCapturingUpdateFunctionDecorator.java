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

package org.gradle.internal.vfs.impl;

import org.gradle.internal.snapshot.AtomicSnapshotHierarchyReference;
import org.gradle.internal.snapshot.SnapshotHierarchy;

import javax.annotation.CheckReturnValue;
import java.util.function.Predicate;

public class DelegatingDiffCapturingUpdateFunctionDecorator implements SnapshotHierarchy.DiffCapturingUpdateFunctionDecorator {

    private final Predicate<String> watchFilter;
    private SnapshotHierarchy.SnapshotDiffListener snapshotDiffListener;
    private ErrorHandler errorHandler;

    public DelegatingDiffCapturingUpdateFunctionDecorator(Predicate<String> watchFilter) {
        this.watchFilter = watchFilter;
    }

    public void setSnapshotDiffListener(SnapshotHierarchy.SnapshotDiffListener snapshotDiffListener, ErrorHandler errorHandler) {
        this.snapshotDiffListener = snapshotDiffListener;
        this.errorHandler = errorHandler;
    }

    public void stopListening() {
        this.snapshotDiffListener = null;
        this.errorHandler = null;
    }

    @Override
    public AtomicSnapshotHierarchyReference.UpdateFunction decorate(SnapshotHierarchy.DiffCapturingUpdateFunction updateFunction) {
        SnapshotHierarchy.SnapshotDiffListener currentListener = snapshotDiffListener;
        if (currentListener == null) {
            return root -> updateFunction.update(root, SnapshotHierarchy.NodeDiffListener.NOOP);
        }

        SnapshotCollectingDiffListener diffListener = new SnapshotCollectingDiffListener(watchFilter);
        return root -> {
            SnapshotHierarchy newRoot = updateFunction.update(root, diffListener);
            return errorHandler.handleErrors(newRoot, () -> diffListener.publishSnapshotDiff(currentListener));
        };
    }

    public interface ErrorHandler {
        @CheckReturnValue
        SnapshotHierarchy handleErrors(SnapshotHierarchy currentRoot, Runnable runnable);
    }
}
