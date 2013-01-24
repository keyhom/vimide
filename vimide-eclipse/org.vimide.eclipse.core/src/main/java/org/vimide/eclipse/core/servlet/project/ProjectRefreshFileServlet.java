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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

/**
 * Request to refresh a file.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/project_refresh_file")
public class ProjectRefreshFileServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ProjectRefreshFileServlet.class);

    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {

        final IProject project = getProject(req);
        final File file = getFile(req);

        // validating the project.
        if (null == project || !project.exists()) {
            resp.sendError(403);
            return;
        }

        // validating the file.
        if (null == file || !file.exists()) {
            resp.sendError(403);
            return;
        }

        // get the file reference in the project and update the file.
        try {
            IFile iFile = project.getFile(file.getAbsolutePath());
            iFile.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (final Exception e) {
            LOGGER.error("Failed to refresh the file: \"{}\" in project: {}",
                    file, project);
        }
        
        // no content response.
    }
}
