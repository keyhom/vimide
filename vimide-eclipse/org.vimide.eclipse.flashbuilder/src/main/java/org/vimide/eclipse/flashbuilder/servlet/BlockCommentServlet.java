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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

/**
 * Requests used to toggle block comment by the selection.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/flexBlockComment")
public class BlockCommentServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(BlockCommentServlet.class
            .getName());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {
        super.doGet(req, resp);

        final IFile file = getProjectFile(getProject(req), getFile(req)
                .getAbsolutePath());

        if (null == file || !file.exists()) {
            resp.sendError(403);
            return;
        }

        int hOffset = req.getIntParameter("hoffset", 0);
        int tOffset = req.getIntParameter("toffset", 0);

        try {
            if (0 < hOffset) {
                hOffset = new FileObject(file.getContents())
                        .getCharLength(hOffset);
            }

            if (0 < tOffset) {
                tOffset = new FileObject(file.getContents())
                        .getCharLength(tOffset);
            }
        } catch (final CoreException e) {
            log.error("", e.getLocalizedMessage(), e);
        }

        Object result = 0;

        if (toggleBlockComment(file, hOffset, tOffset)) {
            result = 1;
        }

        resp.writeAsJson(result);
    }

    protected boolean toggleBlockComment(IFile file, int hOffset, int tOffset) {
        if (null != file && file.exists()) {
            // TODO: toggle the block comment.
        }
        return false;
    }

}

// vim:ft=java
