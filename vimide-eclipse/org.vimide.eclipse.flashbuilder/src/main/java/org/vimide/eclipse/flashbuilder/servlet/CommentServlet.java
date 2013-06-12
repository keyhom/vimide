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
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.adobe.flash.compiler.tree.as.IASNode;
import com.adobe.flash.compiler.tree.as.IFunctionNode;
import com.adobe.flexbuilder.codemodel.common.CMFactory;
import com.adobe.flexbuilder.codemodel.tree.ASOffsetInformation;
import com.adobe.flexide.as.core.ASCorePlugin;
import com.adobe.flexide.as.core.document.ASDocument;
import com.adobe.flexide.editorcore.document.IFlexDocument;

/**
 * Requests used to generate/update the ASDoc comment under the cursor.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/flexComment")
public class CommentServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;
    static final Logger log = LoggerFactory.getLogger(CommentServlet.class
            .getName());

    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {
        final IFile file = getProjectFile(getProject(req), getFile(req)
                .getAbsolutePath());
        if (null == file || !file.exists()) {
            resp.sendError(403);
            return;
        }

        int offset = req.getIntParameter("offset", 0);
        if (0 < offset) {
            try {
                offset = new FileObject(file.getContents())
                        .getCharLength(offset);
            } catch (CoreException e) {
                log.error("", e.getMessage(), e);
            }
        }

        Object result = 0;

        try {
            if (generateASDocComment(file, offset)) {
                result = 1;
            }
        } catch (final Exception e) {
            result = e.getMessage();
            log.error("", e);
        }

        resp.writeAsJson(result);
    }

    /**
     * Generates/Updates the ASDoc comment.
     * 
     * @param file the source file element.
     * @param offset the offset of the cursor.
     * @return true if the ASDoc comment generated/updated, false otherwise.
     * @throws Exception
     */
    protected boolean generateASDocComment(IFile file, int offset)
            throws Exception {
        if (null == file || !file.exists() || 0 > offset)
            return false;

        // Retrieves the document provider.
        ASCorePlugin corePlugin = ASCorePlugin.getDefault();
        IDocumentProvider documentProvider = corePlugin.getDocumentProvider();
        documentProvider.connect(file);
        IFlexDocument doc = (IFlexDocument) documentProvider.getDocument(file);

        if (null != doc) {
            try {
                IRegion info = doc.getLineInformationOfOffset(offset);
                synchronized (CMFactory.getLockObject()) {
                    ASOffsetInformation offsetInfo = ((ASDocument) doc)
                            .getOffsetInformation(offset);

                    IASNode node = offsetInfo
                            .getContainingNodeOfType(IFunctionNode.class);
                    int offsetToUse = info.getOffset();

                    if (null != node) {
                        offsetToUse = doc.getLineInformationOfOffset(
                                ((IFunctionNode) node).getNameStart()).getOffset();
                    }

                    int insertOffset = ((ASDocument) doc)
                            .insertASDocComment(offsetToUse);
                    if (insertOffset > -1) {
                        return true;
                    }
                }
            } finally {
                if (doc.isDirty()) {
                    // save the document.
                    documentProvider.saveDocument(null, file, doc, true);
                }

                doc = null;
                documentProvider.disconnect(file);
            }
        }
        return false;
    }

}

// vim:ft=java
