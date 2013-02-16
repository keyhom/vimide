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
package org.vimide.eclipse.jdt.service;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

/**
 * The basic service for the java functionally service.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class JavaBaseService {

    /**
     * The lazy singleton holder.
     * 
     * @author keyhom (keyhom.c@gmail.com)
     */
    static class SingletonHolder {
        static final JavaBaseService instance = new JavaBaseService();
    }

    /**
     * Gets the singleton instance.
     * 
     * @return singleton.
     */
    public static JavaBaseService getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Gets the compilation unit by the supplied project and file.
     * 
     * @param project the specific project.
     * @param file the specific file belongs to the project.
     * @return the compilation unit element.
     */
    public ICompilationUnit getCompilationUnit(IProject project, File file) {
        if (null != project && null != file) {
            final IPath path = new Path(file.getPath()).makeRelativeTo(project
                    .getLocation());
            if (null != path)
                return getCompilationUnit(project, path);
        }
        return null;
    }

    /**
     * Gets the compilation unit by the supplied project and path.
     * 
     * @param project the specific project.
     * @param path the path of the specific file.
     * @return the compilation unit element.
     */
    public ICompilationUnit getCompilationUnit(IProject project, IPath path) {
        if (null != project && null != path) {
            IFile file = project.getFile(path);
            if (null != file && file.exists()) {
                // refresh locally at first.
                try {
                    file.refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (CoreException e) {
                }
                return JavaCore.createCompilationUnitFrom(file);
            }
        }
        return null;
    }

}
