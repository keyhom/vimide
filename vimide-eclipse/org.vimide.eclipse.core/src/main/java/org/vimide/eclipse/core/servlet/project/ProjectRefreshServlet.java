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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.CoreMessages;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

/**
 * Request to refresh projects.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/project_refresh")
public class ProjectRefreshServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;
    static final Logger LOGGER = LoggerFactory
            .getLogger(ProjectRefreshServlet.class);

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

        final IProject[] projects = getProjects(req);

        if (null == projects || projects.length == 0) {
            resp.sendError(403);
            return;
        }

        final StringBuilder sb = new StringBuilder();

        for (IProject project : projects) {
            if (sb.length() > 0)
                sb.append("\n");

            if (project.exists()) {
                if (!project.isOpen()) {
                    sb.append(NLS.bind(CoreMessages.project_limit_closed_for,
                            project.getName()));
                    continue;
                }
                try {
                    project.refreshLocal(IResource.DEPTH_INFINITE,
                            new NullProgressMonitor());
                    sb.append(NLS.bind(CoreMessages.project_refreshed,
                            project.getName()));

                } catch (CoreException e) {
                    LOGGER.error("Project '{}' refresh failed: {}",
                            project.getName(), e.getMessage(), e);
                    sb.append(NLS.bind(CoreMessages.project_op_failed,
                            "Refreshing", project.getName()));
                }
            } else {
                sb.append(NLS.bind(CoreMessages.project_not_found,
                        project.getName()));
            }
        }

        resp.writeAsJson(sb.toString());
    }

}
