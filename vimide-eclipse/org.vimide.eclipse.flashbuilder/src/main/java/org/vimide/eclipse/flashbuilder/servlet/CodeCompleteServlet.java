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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.adobe.flexbuilder.codemodel.common.CMFactory;
import com.adobe.flexbuilder.codemodel.definitions.IDefinition;
import com.adobe.flexbuilder.codemodel.project.IDocumentSpecification;
import com.adobe.flexbuilder.codemodel.tree.ASOffsetInformation;
import com.adobe.flexbuilder.codemodel.tree.CompletionInformation;
import com.adobe.flexbuilder.codemodel.tree.IFileNode;
import com.google.common.collect.Lists;

/**
 * Requests used to calc the code completions.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/flexComplete")
public class CodeCompleteServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;
    static final Logger log = LoggerFactory.getLogger(CodeCompleteServlet.class
            .getName());

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

        String layout = req.getParameter("layout");

        Object results = calcCodeComplete(project, file, offset, layout);

        if (null == results)
            results = 1;

        resp.writeAsJson(results);
    }

    private Object calcCodeComplete(IProject project, File file, int offset,
            String layout) {
        List<Object> results = Lists.newArrayList();
        synchronized (CMFactory.getLockObject()) {
            IPath filePath = CMFactory.getResourceFromAbsolutePath(
                    file.getAbsolutePath()).getLocation();
            com.adobe.flexbuilder.codemodel.project.IProject asProject = CMFactory
                    .getManager().getProjectForFile(filePath);
            IDocumentSpecification document = CMFactory.getManager()
                    .getDocumentForPath(filePath);
            IFileNode fileNode = asProject.findFileNodeInProject(filePath);
            if (null != fileNode) {
                ASOffsetInformation offsetInformation = new ASOffsetInformation(
                        offset, fileNode);
                
                CompletionInformation completionInformation = offsetInformation.getCompletionInfo(document);
                IDefinition[] definitions = completionInformation
                        .getDefinitions();

                if (null != definitions)
                    for (IDefinition definition : definitions) {
                        log.info("{}", definition.getName());
                        results.add(definition.getQualifiedName());
                    }
            }

        }

        return results;
    }
}

// vim:ft=java
