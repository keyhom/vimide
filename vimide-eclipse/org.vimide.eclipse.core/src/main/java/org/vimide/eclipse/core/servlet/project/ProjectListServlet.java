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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.common.collect.Sets;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/project_list")
public class ProjectListServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;
    static final Logger LOGGER = LoggerFactory
            .getLogger(ProjectListServlet.class);

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

        final List<IProject> results;

        String[] natureIdsFilters = NaturesMapping.getNatureIds(req
                .getParameterValues("natures"));
        Set<String> natureIdsSet = null;

        if (null != natureIdsFilters && natureIdsFilters.length > 0) {
            natureIdsSet = Sets.newHashSet(natureIdsFilters);
        }

        final IProject[] projects = getProjects();

        if (null != natureIdsSet) {
            results = Lists.newArrayList();
            for (IProject project : projects) {
                try {
                    String[] natureIds = project.getDescription()
                            .getNatureIds();
                    for (String natureId : natureIds) {
                        if (natureIdsSet.contains(natureId))
                            results.add(project);
                    }
                } catch (CoreException e) {
                    LOGGER.warn(
                            "Exception at '{}' retrieving the natureIds: {}",
                            project.getName(), e.getMessage());
                }
            }
        } else {
            results = Arrays.asList(projects);
        }

        if (null != natureIdsSet && results.isEmpty())
            resp.writeAsJson(new Object[] {});
        else {
            final List<Map<String, String>> list = Lists.newArrayList();

            for (IProject project : results) {
                if (project.exists()) {
                    Map<String, String> result = Maps.newHashMap();
                    result.put("name", project.getName());
                    result.put("path", project.getLocation().toOSString());
                    result.put("open", Boolean.toString(project.isOpen()));
                    list.add(result);
                }
            }

            resp.writeAsJson(list);
        }
    }

}
