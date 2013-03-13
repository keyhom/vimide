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
package org.vimide.eclipse.jdt.servlet.impl;

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
import org.vimide.eclipse.jdt.manipulation.BeanProperties;
import org.vimide.eclipse.jdt.manipulation.Manipulator;
import org.vimide.eclipse.jdt.service.JavaSourceService;

/**
 * Requests used to generate java bean property methods (getters / setters).
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/javaBeanProperties")
public class JavaGetSetServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Logger.
     */
    static final Logger log = LoggerFactory.getLogger(JavaGetSetServlet.class
            .getName());

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
        if (0 < offset)
            offset = new FileObject(file).getCharLength(offset);

        final int type = req.getIntParameter("type", 0);

        String[] fields = req.getParameterValues("field");
        if (fields == null)
            fields = new String[] {};

        boolean indexed = req.getIntParameter("indexed", 0) != 0 ? true : false;

        // makes something fun here.
        Object result = null;

        final JavaSourceService sourceService = JavaSourceService.getInstance();
        final ICompilationUnit src = sourceService.getCompilationUnit(project,
                file);

        if (null != src && src.exists()) {
            final BeanProperties beanProperties = new BeanProperties(src,
                    offset, type, indexed, fields);
            result = Manipulator.generateProperties(beanProperties);
        }

        if (null == result)
            result = 0;

        resp.writeAsJson(result);
    }
}
