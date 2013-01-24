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
package org.vimide.eclipse.core.servlet.project;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IProject;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.CoreMessages;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

/**
 * Requests to build projects.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/project_build")
public class ProjectBuildServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;
    static final Logger LOGGER = LoggerFactory
            .getLogger(ProjectBuildServlet.class);

    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {

        final boolean all = req.getIntParameter("all") == 0 ? false : true;
        int buildType = req.getIntParameter("type");

        // 6: full build
        // 9: auto build
        // 10: increment build
        // 11: clean build
        if (buildType != 6 || buildType != 9 || buildType != 10
                || buildType != 11) {
            buildType = 9; // set to default mode(auto build).
        }

        final StringBuilder sb = new StringBuilder();

        if (all) {
            try {
                getWorkspace().build(buildType, null);
                sb.append(CoreMessages.project_built_all);
            } catch (final Exception e) {
                LOGGER.error("Error caught at Building all project: {}",
                        e.getMessage(), e);
                sb.append(NLS.bind(CoreMessages.project_op_failed, "Build",
                        "all"));
            }
        } else {

            final IProject[] projects = getProjects(req);
            if (!all && (null == projects || projects.length == 0)) {
                resp.sendError(403);
                return;
            }

            for (IProject project : projects) {
                // perform a project to build.
                try {
                    project.build(buildType, null);
                    sb.append(
                            NLS.bind(CoreMessages.project_built,
                                    project.getName())).append("\n");
                } catch (final Exception e) {
                    LOGGER.error("Error caught at building project: {}",
                            project, e);
                    sb.append(
                            NLS.bind(CoreMessages.project_op_failed,
                                    "Building", project.getName()))
                            .append("\n");
                }
            }
        }

        resp.writeAsJson(sb.toString());
    }
}
