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
package org.vimide.eclipse.flashbuilder.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;
import org.vimide.eclipse.core.util.EclipseResourceUtil;

import com.google.common.collect.Lists;

/**
 * Requests to update the supplied source element.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/flexUpdateSrcFile")
public class FlexUpdateSrcServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Logger.
     */
    static final Logger log = LoggerFactory
            .getLogger(FlexUpdateSrcServlet.class.getName());

    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {
        final IFile ifile = getProjectFile(getProject(req), getFile(req)
                .getAbsolutePath());

        if (null == ifile || !ifile.exists()) {
            resp.sendError(403);
            return;
        }

        final boolean validate = req.getIntParameter("validate", 0) != 0 ? true
                : false;
        final boolean build = req.getIntParameter("build", 0) != 0 ? true
                : false;

        try {
            ifile.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (final CoreException e) {
            log.error("Refresh the file '{}' failed.", ifile);
        }

        if (build) {
            try {
                ifile.getProject().build(
                        IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
            } catch (final CoreException e) {
                log.error("Built failed: {}", ifile, e);
            }
        }

        if (validate) {
            List<Map<String, Object>> results = Lists.newArrayList();

            final List<IMarker> problems = Lists.newArrayList();

            try {
                ifile.accept(new IResourceVisitor() {

                    @Override
                    public boolean visit(final IResource resource)
                            throws CoreException {
                        return problems.addAll(Arrays.asList(ifile
                                .findMarkers(IMarker.PROBLEM, true,
                                        IResource.DEPTH_INFINITE)));
                    }
                }, 0, 0);
            } catch (final CoreException ignore) {
            }

            if (null != problems) {
                String fileName = ifile.getLocation().toOSString()
                        .replace('\\', '/');

                try {

                    for (IMarker problem : problems) {
                        @SuppressWarnings("unchecked")
                        final Map<String, Object> attributes = problem
                                .getAttributes();
                        int sourceStart = (Integer) attributes
                                .get(IMarker.CHAR_START);
                        int sourceEnd = (Integer) attributes
                                .get(IMarker.CHAR_END);
                        int sourceLineNumber = (Integer) attributes
                                .get(IMarker.LINE_NUMBER);
                        boolean isError = ((Integer) attributes
                                .get(IMarker.SEVERITY)) == IMarker.SEVERITY_ERROR ? true
                                : false;
                        int[] pos = new FileObject(ifile.getContents())
                                .getLineColumn(sourceStart);

                        Map<String, Object> m = EclipseResourceUtil
                                .wrapProblemAsMap((String) attributes
                                        .get(IMarker.MESSAGE), sourceStart,
                                        sourceEnd, fileName, sourceLineNumber,
                                        pos[1], isError ? 2 : 1);
                        if (null != m)
                            results.add(m);
                    }
                } catch (final CoreException ignore) {
                    ignore.printStackTrace();
                }
            }

            resp.writeAsJson(results);
            return;
        }

        resp.writeAsJson(1);
    }
}

// vim:ft=java
