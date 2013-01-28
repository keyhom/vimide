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
package org.vimide.eclipse.core.servlet.problem;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Requests to list all the specific level problems.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/problems")
public class ProblemListServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;
    static final Logger LOGGER = LoggerFactory
            .getLogger(ProblemListServlet.class);

    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {

        // Retrieves and validate the supplied projects.
        final IProject[] projects = getProjects(req);

        if (null == projects || projects.length == 0) {
            resp.sendError(403);
            return;
        }

        int severity = req.getIntParameter("severity", 2);

        List<Map<String, Object>> results = Lists.newArrayList();
        int depth = IResource.DEPTH_INFINITE;
        for (IProject project : projects) {
            if (project.exists()) {
                try {
                    IMarker[] findMarkers = project.findMarkers(
                            IMarker.PROBLEM, true, depth);
                    if (null != findMarkers && findMarkers.length > 0) {
                        for (IMarker marker : findMarkers) {
                            Object severityValue = marker
                                    .getAttribute(IMarker.SEVERITY);
                            if (severityValue instanceof Integer
                                    && Integer.parseInt(severityValue
                                            .toString()) <= severity) {
                                Map<String, Object> m = Maps.newHashMap();
//                                m.put("id", String.valueOf(marker.getId()));
//                                m.put("type", marker.getType());
                                m.put("resource", marker.getResource()
                                        .getName());
                                m.put("path", marker.getResource()
                                        .getFullPath().toString());
                                m.put("location", marker.getResource().getLocation().toOSString());
                                Map<String, Object> attributes = marker
                                        .getAttributes();
                                m.putAll(attributes);
                                results.add(m);
                            }
                        }
                    }
                } catch (CoreException e) {
                    LOGGER.error("{}", e.getMessage(), e);
                }
            }
        }

        resp.writeAsJson(results);
    }
}
