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
package org.vimide.eclipse.jdt.servlet.source;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;
import org.vimide.eclipse.jdt.service.JavaSourceService;

/**
 * Requests to
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "javaDocComment")
public class JavaDocCommentServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(JavaDocCommentServlet.class.getName());

    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {

        // Variables usage definition.
        IProject project = null;
        File file = null;
        int offset = 0;
        int offsetLimit = 0;

        // obtains and validate the project argument.
        project = getProject(req);
        if (null == project || !project.exists()) {
            resp.sendError(403);
            return;
        }

        // obtains and validate the file argument.
        file = getFile(req);
        if (null == file || !file.exists()) {
            resp.sendError(403);
            return;
        }

        // obtains the offset argument.
        offset = req.getIntParameter("offset");

        final JavaSourceService service = JavaSourceService.getInstance();
        final ICompilationUnit src = service.getCompilationUnit(project, file);

        // obtains the source content byte length for validating the offset.
        if (null != src && src.exists()) {
            try {
                offsetLimit = src.getBuffer().getContents().getBytes().length;
            } catch (JavaModelException e) {
                LOGGER.error("", e);
            }
        }

        // validate the supplied offset argument.
        if (offsetLimit != 0 && (offset < 0 || offset > offsetLimit)) {
            resp.sendError(403);
            return;
        }

        IJavaElement element = null;
        try {
            element = src.getElementAt(offset);
        } catch (JavaModelException e) {
            LOGGER.error("", e);
        }

        // filtering the import declarations.
        if (null != element
                && element.getElementType() != IJavaElement.IMPORT_DECLARATION) {
            // Generate the covering element comment.
            service.generateElementComment(src, element);
        }

        resp.writeAsJson(1);
    }

}
