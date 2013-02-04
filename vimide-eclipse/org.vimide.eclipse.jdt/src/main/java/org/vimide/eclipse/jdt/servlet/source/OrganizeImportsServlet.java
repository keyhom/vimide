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

import org.eclipse.core.resources.IProject;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

/**
 * Requests to organize imports for the specific source.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class OrganizeImportsServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {
        // Validating the supplied request parameters.
        final IProject project = getProject(req);
        
        if (null == project || !project.exists()) {
            resp.sendError(403);
            return;
        }
        
        final File file = getFile(req);
        
        if (null == file || !file.exists() || file.isDirectory()) {
            resp.sendError(403);
            return;
        }
        
        // Be sure the project and the file is correct.
        
    }

}
