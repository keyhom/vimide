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
package org.vimide.eclipse.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.vimide.core.util.FileObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The utilities for the eclipse resources.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class EclipseResourceUtil {

    public static Map<String, Object> wrapProblemAsMap(String message,
            int charStart, int charEnd, String fileName, int line, int col,
            int severity) {
        Map<String, Object> m = Maps.newHashMap();
        m.put("message", message);
        m.put("charStart", charStart);
        m.put("charEnd", charEnd);
        m.put("filename", fileName);
        m.put("line", line);
        m.put("col", col);
        m.put("severity", severity);
        return m;
    }

    @SuppressWarnings("unchecked")
	public static Map<String, Object> getProblem(IMarker marker)
            throws CoreException, FileNotFoundException {
        if (null == marker)
            return null;

        IResource resource = marker.getResource();
        if (null == resource || null == resource.getRawLocation()) {
            return null;
        }

        Map<String, Object> attributes = marker.getAttributes();
        Object severityValue = attributes.get(IMarker.SEVERITY);

        int offset = attributes.containsKey("charStart") ? ((Integer) attributes
                .get("charStart")).intValue() : 1;
        int line = attributes.containsKey("lineNumber") ? ((Integer) attributes
                .get("lineNumber")).intValue() : 1;

        int[] pos = { 1, 1 };

        String message = (String) attributes.get("message");
        String path = resource.getLocation().toOSString().replace("\\", "/");

        File file = new File(path);
        if (file.isFile() && file.exists() && offset > 0) {
            pos = new FileObject(file).getLineColumn(offset);
        }

        int charStart = ((Integer) attributes.get(IMarker.CHAR_START))
                .intValue();
        int charEnd = ((Integer) attributes.get(IMarker.CHAR_END)).intValue();
        int severity = ((Integer) severityValue).intValue();
        return wrapProblemAsMap(message, charStart, charEnd, path, line,
                pos[1], severity);
    }

    public static List<Map<String, Object>> getProblems(IMarker[] markers) {
        return getProblems(markers, 2); // get all problems.
    }

    public static List<Map<String, Object>> getProblems(IMarker[] markers,
            int severityLevel) {
        if (null != markers && markers.length > 0) {
            final List<Map<String, Object>> lists = Lists.newArrayList();
            for (IMarker marker : markers) {
                try {
                    int severityValue = ((Integer) marker
                            .getAttribute(IMarker.SEVERITY)).intValue();
                    if (severityValue <= severityValue) {
                        Map<String, Object> result = getProblem(marker);
                        if (null != result)
                            lists.add(result);
                    }
                } catch (final Exception ignore) {
                    ignore.printStackTrace();
                }
            }

            return lists;
        }
        return null;
    }

}
