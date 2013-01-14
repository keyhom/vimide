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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IProject;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.google.common.collect.Lists;

/**
 * Requests the project's name as collection.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/project_names")
public class ProjectNamesServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

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
        
        final List<String> names = Lists.newArrayList();
        final IProject[] projects = getProjects();
        
        for (IProject project : projects) {
            names.add(project.getName());
        }
        
        resp.writeAsJson(names);
    }

}
