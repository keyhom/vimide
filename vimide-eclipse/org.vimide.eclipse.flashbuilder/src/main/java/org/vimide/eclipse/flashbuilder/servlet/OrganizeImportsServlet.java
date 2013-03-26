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
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.adobe.flexide.as.core.ASCorePlugin;
import com.adobe.flexide.as.core.document.IASModel;
import com.adobe.flexide.editorcore.document.IFlexDocument;

/**
 * Requests used to organize imports (removed unused).
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/flexOrganizeImports")
public class OrganizeImportsServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
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

        Object result = 0;
        try {
            if (organizeImports(file)) {
                result = 1;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        resp.writeAsJson(result);
    }

    protected boolean organizeImports(final IFile file) throws Exception {
        if (null != file && file.exists()) {
            final ASCorePlugin corePlugin = ASCorePlugin.getDefault();
            IDocumentProvider documentProvider = corePlugin
                    .getDocumentProvider();

            IFlexDocument doc = null;

            if (null != documentProvider) {

                try {
                    doc = (IFlexDocument) documentProvider.getDocument(file);
                    DocumentRewriteSession session = doc
                            .startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);

                    try {
                        ((IASModel) doc).organizeImports(isRemoveUnused());

                        return true;
                    } finally {
                        doc.stopRewriteSession(session);
                    }
                } finally {
                    if (null != doc && doc.isDirty()) {
                        documentProvider.saveDocument(null, file, doc, true);
                    }
                    doc = null;
                    documentProvider.disconnect(file);
                }
            }
        }
        return false;
    }

    private boolean isRemoveUnused() {
        return ASCorePlugin.getDefault().getPreferenceStore()
                .getBoolean("removeUnused");
    }

}

// vim:ft=java
