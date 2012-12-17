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
package org.vimide.eclipse.core.servlet.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.CoreMessages;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.google.common.base.Strings;

/**
 * Request to import projects.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/project_import")
public class ProjectImportServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    static final Logger LOGGER = LoggerFactory
            .getLogger(ProjectImportServlet.class);

    /**
     * {@inheritDoc}
     * 
     * @see org.vimide.core.servlet.VimideHttpServlet#doGet(org.vimide.core.servlet.VimideHttpServletRequest,
     *      org.vimide.core.servlet.VimideHttpServletResponse)
     */
    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {

        final File file = getFile(req);

        if (null == file || !file.exists()) {
            resp.sendError(403);
            return;
        }

        final StringBuilder sb = new StringBuilder();

        final File dotProject = new File(file, ".project");

        if (dotProject.exists()) {
            // It's a valid eclipse project.
            IProjectDescription description = null;
            try {
                description = getWorkspace().loadProjectDescription(
                        new FileInputStream(dotProject));
            } catch (CoreException e) {
                LOGGER.error("Failed to reading project description file: {}",
                        dotProject.getAbsolutePath(), e);
            }

            if (null != description
                    && !Strings.isNullOrEmpty(description.getName())) {
                String name = description.getName();

                IProject project = getWorkspace().getRoot().getProject(name);
                if (project.exists()) {
                    // already exists.
                    sb.append(NLS.bind(CoreMessages.project_name_exists,
                            project.getName()));
                    resp.writeAsJson(sb.toString());
                    return;
                } else {
                    IPath path = new Path(file.getAbsolutePath());
                    description.setLocation(path);

                    try {
                        project.create(description, new NullProgressMonitor());
                        sb.append(NLS.bind(CoreMessages.project_imported,
                                project.getName()));
                    } catch (final CoreException e) {
                        LOGGER.error("Imported project '{}' faild: {}",
                                project.getName(), e.getMessage(), e);
                        sb.append(NLS.bind(CoreMessages.project_import_failed,
                                project.getName()));
                    }
                }
            }
        } else {
            sb.append(NLS.bind(CoreMessages.project_dotproject_missing,
                    file.getAbsolutePath()));
        }

        resp.writeAsJson(sb.toString());
    }

}
