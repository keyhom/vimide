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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * The utilities for eclipse project.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class EclipseProjectUtil {

    /**
     * Determines and return the line delimiter of the specific project.
     * 
     * @param project the specific project.
     * @return the line delimiter.
     */
    public static String getProjectLineDelimiter(IProject project) {
        IScopeContext[] scopeContext = null;
        if (null != project) {
            // project preference.
            scopeContext = new IScopeContext[] { new ProjectScope(project) };

            String lineDelimiter = Platform.getPreferencesService().getString(
                    Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null,
                    scopeContext);
            if (null != lineDelimiter)
                return lineDelimiter;
        }

        // workspace preference.
        scopeContext = new IScopeContext[] { new InstanceScope() };
        String platformDefault = System.getProperty("line.separator", "\n");
        return Platform.getPreferencesService().getString(Platform.PI_RUNTIME,
                Platform.PREF_LINE_SEPARATOR, platformDefault, scopeContext);
    }

}
