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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.natures.NaturesMapping;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Requests to inspect the information of project.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/project_info")
public class ProjectInfoServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ProjectInfoServlet.class);

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

        final IProject project = getProject(req);

        if (null != project && project.exists()) {
            String workspace = getWorkspace().getRoot().getRawLocation()
                    .toOSString();

            Map<String, Object> info = Maps.newHashMap();
            info.put("name", project.getName());
            info.put("path", project.getLocation().toOSString());
            info.put("workspace", workspace);
            info.put("open", Boolean.toString(project.isOpen()));

            if (project.isOpen()) {
                try {
                    String[] aliases = NaturesMapping.getNatureAliases(project
                            .getDescription().getNatureIds());
                    if (null == aliases || aliases.length == 0)
                        aliases = new String[] { "none" };

                    info.put("natures", aliases);

                    IProject[] depends = project.getReferencedProjects();

                    if (depends.length > 0) {
                        List<String> names = Lists.newArrayList();
                        for (IProject depend : depends) {
                            names.add(depend.getName());
                        }

                        info.put("depends", names);
                    }

                    IProject[] references = project.getReferencingProjects();
                    if (references.length > 0) {
                        List<String> names = Lists.newArrayList();
                        for (IProject reference : references) {
                            names.add(reference.getName());
                        }

                        info.put("references", names);
                    }
                } catch (CoreException e) {
                    LOGGER.warn(
                            "Exception at '{}' retrieving the project natureIds: {}",
                            project.getName(), e.getMessage());
                }
            }

            resp.writeAsJson(info);
        } else {
            resp.sendError(403);
        }
    }

}
