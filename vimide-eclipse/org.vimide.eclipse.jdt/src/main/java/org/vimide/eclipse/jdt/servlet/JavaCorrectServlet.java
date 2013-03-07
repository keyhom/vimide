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
package org.vimide.eclipse.jdt.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;
import org.vimide.eclipse.jdt.correct.JavaCorrectService;

/**
 * Handles requests for code corrections.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/javaCorrect")
public class JavaCorrectServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Logger
     */
    static final Logger log = LoggerFactory.getLogger(JavaCorrectServlet.class
            .getName());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {
        // parameters validating.
        IProject project = getProject(req);
        if (null == project || !project.exists()) {
            resp.sendError(403);
            return;
        }

        File file = getFile(req);
        if (null == file || !file.exists()) {
            resp.sendError(403);
            return;
        }

        int line = req.getIntParameter("line", 0);
        if (0 >= line) {
            resp.sendError(403);
            return;
        }

        int offset = req.getIntParameter("offset", 0);
        if (0 < offset)
            offset = new FileObject(file).getCharLength(offset);
        else if (0 >= offset) {
            resp.sendError(403);
            return;
        }

        int apply = req.getIntParameter("apply", -1);

        final JavaCorrectService service = JavaCorrectService.getInstance();
        final ICompilationUnit src = service.getCompilationUnit(project, file);

        Object result = null;

        if (null != src && src.exists()) {
            try {
                result = service.makeCorrect(src, line, offset, apply);
                if (null == result)
                    result = 1;
            } catch (final Exception e) {
                log.error("{}", e.getMessage(), e);
            }
        }

        resp.writeAsJson(null == result ? 0 : result);
    }
}
