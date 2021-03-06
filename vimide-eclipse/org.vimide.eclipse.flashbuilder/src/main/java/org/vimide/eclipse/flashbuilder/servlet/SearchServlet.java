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
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.adobe.flexbuilder.codemodel.search.SearchScope;
import com.adobe.flexbuilder.codemodel.search.SearchScope.SearchContext;
import com.adobe.flexide.as.core.ASCorePlugin;
import com.adobe.flexide.editorcore.document.IFlexDocument;

/**
 * Requests used to search Flex element.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/flexSearch")
public class SearchServlet extends GenericVimideHttpServlet {

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

        int offset = req.getIntParameter("offset", 0);
        if (0 < offset)
            try {
                offset = new FileObject(file.getContents())
                        .getCharLength(offset);
            } catch (final CoreException ignore) {
            }

        String scope = req.getNotNullParameter("scope");
        int length = req.getIntParameter("length");
        String type = req.getNotNullParameter("type");
        boolean caseSensitive = req.getIntParameter("caseSensitive", 1) != 0 ? true
                : false;
        String pattern = req.getNotNullParameter("pattern");

        try {
            Collection<?> matches = collectMatches(file, offset, length,
                    caseSensitive, type, scope, pattern);

            resp.writeAsJson(matches);
        } catch (final Exception e) {

        }
    }

    protected Collection<?> collectMatches(IFile file, int offset, int length,
            boolean caseSensitive, String type, String scope, String pat)
            throws Exception {

        if (null == file)
            return null;

        // Retrieves document by ASCore.
        ASCorePlugin corePlugin = ASCorePlugin.getDefault();
        IDocumentProvider documentProvider = corePlugin.getDocumentProvider();
        documentProvider.connect(file);
        IFlexDocument document = (IFlexDocument) documentProvider
                .getDocument(file);

        try {
            int context = -1;
            SearchScope searchScope = SearchScope.createProjectScope(file
                    .getLocation());
            SearchContext searchContext = searchScope.getContext();

            // if (null != definition) {
            // boolean imported = false;
            // List<String> allImports = ((IASModel) document)
            // .getAllImports(offset);
            // for (String importedType : allImports) {
            // if (importedType.equals(definition
            // .getDefinitionQualifiedName())) {
            // imported = true;
            // }
            // }

            // if (!imported) {
            // ((IASModel) document).createImportElement().setImportName(
            // definition.getDefinitionQualifiedName());
            // }
            // }

            return null;
        } finally {
            if (null != document) {
                document = null;
                documentProvider.disconnect(file);
            }
        }
    }
}

// vim:ft=java
