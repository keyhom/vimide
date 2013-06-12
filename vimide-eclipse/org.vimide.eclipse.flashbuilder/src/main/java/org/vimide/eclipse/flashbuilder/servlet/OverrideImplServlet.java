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
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.adobe.flash.compiler.definitions.IClassDefinition;
import com.adobe.flash.compiler.definitions.IDefinition;
import com.adobe.flash.compiler.tree.as.IClassNode;
import com.adobe.flash.compiler.tree.as.IFileNode;
import com.adobe.flexbuilder.as.editor.core.OverrideMethodsProcessor;
import com.adobe.flexbuilder.codemodel.common.CMFactory;
import com.adobe.flexbuilder.codemodel.tree.ASOffsetInformation;
import com.adobe.flexide.as.core.document.IASModel;

/**
 * Requests to make a overridden/implementation manipulation.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/flexImpl")
public class OverrideImplServlet extends GenericVimideHttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(final VimideHttpServletRequest req,
			final VimideHttpServletResponse resp) throws ServletException,
			IOException {
		final IFile file = getProjectFile(getProject(req), getFile(req)
				.getAbsolutePath());

		if (null == file || !file.exists()) {
			resp.sendError(403);
			return;
		}

		Runnable runner = new Runnable() {

			@Override
			public void run() {
				Object[] arrayOfObject1;
				OverrideMethodsProcessor processor = createProcessor();

				if (null == processor)
					return;

				OverrideMethodsProcessor.OverrideMethodsProcessorNode[] ancestory = (OverrideMethodsProcessor.OverrideMethodsProcessorNode[]) null;
				ancestory = processor.getAvailableMethods();

				if (ancestory.length == 0) {
					// XXX: no methods to make an overridden/implemetation.
					return;
				}

			}

		};

		runner.run();
	}

	protected OverrideMethodsProcessor createProcessor() {
		IASModel model = null;
		OverrideMethodsProcessor processor = null;

		synchronized (CMFactory.getLockObject()) {
			IClassDefinition cls = null;
			int offset = 0;
		}
		return null;
	}

	protected IClassDefinition computeTargetClass(IASModel model, int offset) {
		IFileNode fileNode = (IFileNode) model.getBaseFileNode();
		ASOffsetInformation info = new ASOffsetInformation(offset, fileNode);
		IClassDefinition cls = ((IClassNode) info
				.getContainingNodeOfType(IClassNode.class)).getDefinition();

		if (null != cls) {
			return cls;
		}

		return computeTargetClassFromFile(fileNode);
	}

	protected IClassDefinition computeTargetClassFromFile(IFileNode fileNode) {
		IDefinition[] arrayOfIDefinition1;
		IDefinition[] allTopLevelDefinitions = fileNode.getTopLevelDefinitions(
				false, true);

		int j = (arrayOfIDefinition1 = allTopLevelDefinitions).length;
		for (int i = 0; i < j; ++i) {
			IDefinition def = arrayOfIDefinition1[i];
			if (def instanceof IClassDefinition) {
				return ((IClassDefinition) def);
			}
		}
		return null;
	}

}

// vim:ft=java
