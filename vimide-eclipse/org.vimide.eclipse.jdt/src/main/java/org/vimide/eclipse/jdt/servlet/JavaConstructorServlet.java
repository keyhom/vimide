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
import org.eclipse.jdt.core.IType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;
import org.vimide.eclipse.jdt.service.JavaSourceService;
import org.vimide.eclipse.jdt.util.TypeUtil;

/**
 * Requests used to generate class constructors.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/javaConstructor")
public class JavaConstructorServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Logger
     */
    static final Logger log = LoggerFactory
            .getLogger(JavaConstructorServlet.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {
        final IProject project = getProject(req);
        if (null == project || !project.exists()) {
            resp.sendError(403);
            return;
        }

        final File file = getFile(req);
        if (null == file || !file.exists()) {
            resp.sendError(403);
            return;
        }

        int offset = req.getIntParameter("offset", 0);
        if (0 < offset) {
            offset = new FileObject(file).getCharLength(offset);
        }

        boolean omitSuper = req.getIntParameter("super", 0) == 0 ? false : true;

        final JavaSourceService sourceService = JavaSourceService.getInstance();
        final ICompilationUnit src = sourceService.getCompilationUnit(project,
                file);

        Object result = 0;

        if (null != src && src.exists()) {
            try {
                IType type = TypeUtil.getType(src, offset);
                String[] fields = req.getParameterValues("field");
                if (null == fields)
                    fields = new String[] {};
                result = sourceService.generateConstructor(src, type,
                        !omitSuper, fields);
                if (null == result)
                    result = 1;
            } catch (Exception e) {
                log.error("", e);
            }
        }

        resp.writeAsJson(result);
    }
}
