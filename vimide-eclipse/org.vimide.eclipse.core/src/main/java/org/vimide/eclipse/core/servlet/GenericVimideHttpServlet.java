/**
 * Copyright (c) 2012 keyhom.c@gmail.com.
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
package org.vimide.eclipse.core.servlet;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.vimide.core.servlet.VimideHttpServlet;
import org.vimide.core.servlet.VimideHttpServletRequest;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * An generic implementation of Vimide' http servlet.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public abstract class GenericVimideHttpServlet extends VimideHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String REQ_PROJECT = "project";
    private static final String REQ_FILE = "file";

    protected String getOSEncoding() {
        String encoding = System.getProperty("sun.jnu.encoding");
        if (Strings.isNullOrEmpty(encoding))
            encoding = Charset.defaultCharset().name();
        return encoding;
    }

    protected File getFile(VimideHttpServletRequest req) {
        String reqFile = req.getNotNullParameter(REQ_FILE);
        if (!reqFile.isEmpty()) {
            return new File(reqFile);
        }
        return null;
    }

    protected File[] getFiles(VimideHttpServletRequest req) {
        String[] reqFiles = req.getNotNullParameterValues(REQ_FILE);
        List<File> files = null;
        if (null != reqFiles && reqFiles.length > 0) {
            files = Lists.newArrayList();
            for (String reqFile : reqFiles) {
                files.add(new File(reqFile));
            }
        }
        return null == files ? null : files.toArray(new File[files.size()]);
    }

    protected IProject getProject(VimideHttpServletRequest req) {
        String reqProject = req.getNotNullParameter(REQ_PROJECT);
        if (!reqProject.isEmpty()) {
            return getWorkspace().getRoot().getProject(reqProject);
        }
        return null;
    }

    protected IProject[] getProjects(VimideHttpServletRequest req) {
        List<IProject> projects = null;
        String[] values = req.getNotNullParameterValues(REQ_PROJECT);
        if (null != values && values.length > 0) {
            projects = Lists.newArrayList();
            for (String value : values) {
                projects.add(getWorkspace().getRoot().getProject(value));
            }
        }
        return null == projects ? null : projects.toArray(new IProject[projects
                .size()]);
    }

    protected IProject[] getProjects() {
        return getWorkspace().getRoot().getProjects();
    }

    protected IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    protected IFile getProjectFile(IProject project, String filePath) {
        if (null == project || !project.exists() || Strings.isNullOrEmpty(filePath)) {
            return null;
        }

        IPath path = new Path(filePath).makeRelativeTo(project.getLocation());
        return project.getFile(path);
    }

}
