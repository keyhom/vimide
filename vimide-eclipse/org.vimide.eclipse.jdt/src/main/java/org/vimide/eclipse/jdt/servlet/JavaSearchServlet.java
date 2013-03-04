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

package org.vimide.eclipse.jdt.servlet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.core.util.Position;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;
import org.vimide.eclipse.jdt.service.JavaSearchService;

import com.google.common.collect.Lists;

/**
 * Requests to handle java search requests.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/javaSearch")
public class JavaSearchServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * LOGGER.
     */
    private static final Logger log = LoggerFactory
            .getLogger(JavaSearchServlet.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {

        // determines the project is valid.
        final IProject project = getProject(req);
        if (null == project || !project.exists()) {
            resp.sendError(403);
            return;
        }

        // determines the file is valid.
        final File file = getFile(req);
        if (null == file || !file.exists()) {
            resp.sendError(403);
            return;
        }

        // convert the bytes offset to char offset.
        int offset = req.getIntParameter("offset");

        if (offset > 0) {
            offset = new FileObject(file).getCharLength(offset);
        }

        // receives other parameters.
        String scope = req.getNotNullParameter("scope");
        int length = req.getIntParameter("length");
        String type = req.getNotNullParameter("type");
        boolean caseSensitive = req.getIntParameter("caseSensitive", 1) != 0 ? true
                : false;
        String pattern = req.getNotNullParameter("pattern");

        final JavaSearchService service = JavaSearchService.getInstance();
        final ICompilationUnit src = service.getCompilationUnit(project, file);
        try {
            List<SearchMatch> matches = service.collectMatches(src, offset,
                    length, caseSensitive, service.getType(type), scope, pattern);

            List<Position> results = Lists.newArrayList();
            for (SearchMatch match : matches) {
                IJavaElement element = (IJavaElement) match.getElement();
                if (null != element) {
                    int elementType = element.getElementType();
                    if (elementType != IJavaElement.PACKAGE_FRAGMENT
                            && elementType != IJavaElement.PACKAGE_FRAGMENT_ROOT) {
                        Position result = service
                                .createPosition(project, match);
                        if (null != result)
                            results.add(result);
                    }
                }
            }

            resp.writeAsJson(results);
        } catch (final Exception e) {
            log.error("Error caught at searching: {}", e.getMessage(), e);
            resp.writeAsJson(e.getMessage());
            return;
        }
    }

}
