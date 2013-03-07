/**
 * Copyright (c) 2013 keyhom.c@gmail.com.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose
 * excluding commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *     1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *     2. Altered source versions must be plainly marked as such, and must not
 *     be misrepresented as being the original software.
 *
 *     3. This notice may not be removed or altered from any source
 *     distribution.
 */
package org.vimide.eclipse.core.refactoring;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Resource change listener which can be used to collect a list of relevant
 * resource deltas and generate a list of file moves, renames, etc when applying
 * a Refactoring or Change.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class ResourceChangeListener implements IResourceChangeListener,
        IResourceDeltaVisitor {

    private List<IResourceDelta> deltas = Lists.newArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        try {
            event.getDelta().accept(this);
        } catch (CoreException ce) {
            throw new RuntimeException(ce);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(IResourceDelta delta) throws CoreException {
        IResource resource = delta.getResource();
        if (delta.getKind() != IResourceDelta.NO_CHANGE
                && (IResource.FILE == resource.getType() || IResource.FOLDER == resource
                        .getType())) {
            deltas.add(delta);
        }
        return true;
    }

    /**
     * Gets a list of relavant leaf node resource deltas.
     * 
     * @return list of IResourceDelta.
     */
    public List<IResourceDelta> getResourceDeltas() {
        return deltas;
    }

    /**
     * Builds a list of changed files which can be returned from a command
     * informing the client of files changed, moved, etc.
     * 
     * @return a list of changed files.
     */
    public List<Map<String, String>> getChangedFiles() {
        List<Map<String, String>> results = Lists.newArrayList();
        Set<String> seen = Sets.newHashSet();

        for (IResourceDelta delta : getResourceDeltas()) {
            int flags = delta.getFlags();
            // the moved_from entry should handle this.
            if ((flags & IResourceDelta.MOVED_TO) != 0) {
                continue;
            }

            Map<String, String> result = Maps.newHashMap();

            IResource resource = delta.getResource();
            String file = resource.getLocation().toOSString()
                    .replace('\\', '/');

            if ((flags & IResourceDelta.MOVED_FROM) != 0) {
                String path = delta.getMovedFromPath().toOSString();
                result.put("from", path);
                result.put("to", file);
            } else if (resource.getLocation().getFileExtension() != null) {
                if (!seen.contains(file)) {
                    result.put("file", file);
                    seen.add(file);
                }
            }

            if (!result.isEmpty()) {
                results.add(result);
            }
        }
        return results;
    }
}
