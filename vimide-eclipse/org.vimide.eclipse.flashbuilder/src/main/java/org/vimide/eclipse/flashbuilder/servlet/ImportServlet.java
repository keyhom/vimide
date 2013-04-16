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
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.core.util.Position;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.adobe.flexide.as.core.ASCorePlugin;
import com.adobe.flexide.as.core.document.IASModel;
import com.adobe.flexide.editorcore.document.IFlexDocument;
import com.google.common.base.Strings;

/**
 * Requests used to import the specific type.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/flexImport")
public class ImportServlet extends GenericVimideHttpServlet {

	private static final long serialVersionUID = 1L;
	static final Logger log = LoggerFactory.getLogger(ImportServlet.class
			.getName());

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(VimideHttpServletRequest req,
			VimideHttpServletResponse resp) throws ServletException,
			IOException {
		final IFile file = getProjectFile(getProject(req), getFile(req)
				.getAbsolutePath());

		if (null == file) {
			resp.sendError(403);
			return;
		}

		int offset = req.getIntParameter("offset", 0);
		if (0 < offset) {
			offset = new FileObject(file.getLocation().toOSString())
					.getCharLength(offset);
		}

		String type = req.getParameter("type");

		try {
			Object result = importType(file, offset, type);
			if (null == result)
				result = 1;
			resp.writeAsJson(result);
		} catch (Exception e) {
			log.error("Import type - '{}' cause error: {}", type,
					e.getMessage(), e);
			resp.writeAsJson(e.getMessage());
		}
	}

	protected Object importType(IFile file, int offset, String typeName)
			throws Exception {
		// Determines whether the typeName suppling null was.
		if (!Strings.isNullOrEmpty(typeName)) {

			ASCorePlugin corePlugin = ASCorePlugin.getDefault();
			IDocumentProvider documentProvider = corePlugin
					.getDocumentProvider();
			// connects the file.
			documentProvider.connect(file);
			// retrieves the actionscript document.
			IFlexDocument document = (IFlexDocument) documentProvider
					.getDocument(file);

			try {
				((IASModel)document).insertImport(typeName, offset);
				((IASModel)document).organizeImports(false);
			} finally {
				if (document.isDirty()) {
					// save the document.
					documentProvider.saveDocument(null, file, document, true);
				}

				if (null != document) {
					document = null;
					documentProvider.disconnect(file);
				}
			}
		} else {

        }

		return Position.fromOffset(file.getLocation().toOSString(), null,
				offset + typeName.length() + 10, 0);
	}
}

// vim:ft=java
