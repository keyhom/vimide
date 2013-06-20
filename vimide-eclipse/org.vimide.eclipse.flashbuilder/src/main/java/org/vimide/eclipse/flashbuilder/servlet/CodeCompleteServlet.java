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
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.complete.CodeCompletionResult;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;
import org.vimide.eclipse.flashbuilder.complete.CodeCompletionResponse;
import org.vimide.eclipse.flashbuilder.search.SearchManager;
import org.vimide.eclipse.jface.text.DummyTextViewer;

import com.adobe.flash.compiler.definitions.IDefinition;
import com.adobe.flash.compiler.tree.as.IASNode;
import com.adobe.flash.compiler.tree.as.IClassNode;
import com.adobe.flash.compiler.tree.as.IIdentifierNode;
import com.adobe.flash.compiler.tree.as.IMemberAccessExpressionNode;
import com.adobe.flexbuilder.codemodel.common.CMFactory;
import com.adobe.flexbuilder.codemodel.tree.ASOffsetInformation;
import com.adobe.flexide.as.core.ASCorePlugin;
import com.adobe.flexide.as.core.IASDataProvider;
import com.adobe.flexide.as.core.contentassist.ActionScriptCompletionProcessor;
import com.adobe.flexide.as.core.contentassist.ActionScriptCompletionProposal;
import com.adobe.flexide.as.core.document.IASModel;
import com.adobe.flexide.editorcore.contentassist.FlexCompletionProposal;
import com.adobe.flexide.editorcore.document.IFlexDocument;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
		final IFile file = getProjectFile(getProject(req), getFile(req)
				.getAbsolutePath());

		if (null == file) {
			resp.sendError(404);
			return;
		}

		int offset = req.getIntParameter("offset", 0);
		if (0 < offset) {
			try {
				offset = new FileObject(file.getContents())
						.getCharLength(offset);
			} catch (CoreException ignore) {
			}
		}

		// String layout = req.getParameter("layout");

		CodeCompletionResponse response = null;
		List<String> imports = null;

		try {
			imports = isNeedImport(file, offset);
			if (null != imports && !imports.isEmpty()) {
				response = new CodeCompletionResponse(null, null, imports);
			} else {
				List<CodeCompletionResult> result = calculateCompletion(file,
						offset);
				// if (null == result || result.isEmpty()) {
				// // collection import
				// Collection<IDefinition> definitions = getImports(file,
				// offset);
				// if (null != definitions && !definitions.isEmpty()) {
				// imports = Lists.newArrayList();
				// for (IDefinition def : definitions) {
				// imports.add(def.getQualifiedName());
				// }
				// }
				// }

				response = new CodeCompletionResponse(result, null, imports);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		resp.writeAsJson(null != response ? response.toMap() : Maps
				.newHashMap());
	}

	List<String> isNeedImport(IFile file, int offset) throws Exception {
		if (null != file) {

			List<String> results = Lists.newArrayList();

			ASCorePlugin corePlugin = ASCorePlugin.getDefault();
			IDocumentProvider documentProvider = corePlugin
					.getDocumentProvider();
			// connects the file.
			documentProvider.connect(file);
			// retrieves the actionscript document.
			IFlexDocument document = (IFlexDocument) documentProvider
					.getDocument(file);

			ASOffsetInformation offsetInfo = null;
			IASNode containingNode = null;
			IASDataProvider dataProvider = null;
			IIdentifierNode node = null;

			try {
				synchronized (CMFactory.getLockObject()) {
					if (document instanceof IASDataProvider) {
						dataProvider = (IASDataProvider) document;
					}

					if (null == dataProvider)
						return null;

					offsetInfo = dataProvider.getOffsetInformation(offset);
					if (null == offsetInfo)
						return null;
					containingNode = offsetInfo.getContainingNode();
				}

				if (null == containingNode)
					return null;

				if (!(containingNode instanceof IMemberAccessExpressionNode)) {
					containingNode = containingNode
							.getAncestorOfType(IMemberAccessExpressionNode.class);
				}

				if (null != containingNode
						&& containingNode instanceof IMemberAccessExpressionNode) {
					if (((IMemberAccessExpressionNode) containingNode)
							.getLeftOperandNode() instanceof IIdentifierNode) {
						node = (IIdentifierNode) ((IMemberAccessExpressionNode) containingNode)
								.getLeftOperandNode();
					}
				}

				if (null != node) {
//						&& (node.getPackageName().isEmpty() || !node.getPackageName().equals(
//								((IClassNode) node
//										.getAncestorOfType(IClassNode.class))
//										.getDefinition().getPackageName()))) {
					// definition found, check if imported needed.
					offset = node.getEnd() - 1;
					// get completions.
					ITextViewer textViewer = new DummyTextViewer(document, 0, 0);
					ActionScriptCompletionProcessor processor = new ActionScriptCompletionProcessor();
					List<String> allImports = ((IASModel) document)
							.getAllImports(offset);

					// IContextInformation[] computeContextInformation =
					// processor
					// .computeContextInformation(textViewer, offset);
					// processor.getProposalState()
					if (processor.hasCompletionProposals(textViewer, offset)) {
						ICompletionProposal[] proposals = processor
								.computeCompletionProposals(textViewer, offset);
						for (ICompletionProposal proposal : proposals) {
							if (proposal instanceof FlexCompletionProposal) {
								ActionScriptCompletionProposal lazy = (ActionScriptCompletionProposal) proposal;
								if (lazy.getReplacementString().equals(
										node.getName())) {
									Field addImport = ActionScriptCompletionProposal.class
											.getDeclaredField("fAddImport");
									boolean accessibleFlag = addImport
											.isAccessible();
									addImport.setAccessible(true);
									boolean importing = addImport
											.getBoolean(lazy);
									addImport.setAccessible(accessibleFlag);

									if (importing) {
										boolean isImported = false;
										for (String imprt : allImports) {
											if (imprt.equals(lazy
													.getQualifiedName())) {
												isImported = true;
											}
										}

										if (!isImported) {
											if (!Strings.isNullOrEmpty(lazy
													.getQualifiedName())) {
												results.add(lazy
														.getQualifiedName());
											}
										}
									}
								}
							}
						}
					}

					return results;
				}

			} finally {
				if (null != document) {
					document = null;
					documentProvider.disconnect(file);
				}
			}
		}

		return null;
	}

	Collection<IDefinition> getImports(IFile file, int offset) throws Exception {
		ASCorePlugin corePlugin = ASCorePlugin.getDefault();
		IDocumentProvider documentProvider = corePlugin.getDocumentProvider();
		// connects the file.
		documentProvider.connect(file);
		// retrieves the actionscript document.
		IFlexDocument document = (IFlexDocument) documentProvider
				.getDocument(file);

		Collection<IDefinition> results = Lists.newArrayList();
		ASOffsetInformation offsetInfo = null;
		IASNode containingNode = null;
		IASDataProvider dataProvider = null;

		try {

			synchronized (CMFactory.getLockObject()) {
				if (document instanceof IASDataProvider) {
					dataProvider = (IASDataProvider) document;
				}

				if (null == dataProvider)
					return null;

				offsetInfo = dataProvider.getOffsetInformation(offset);
				if (null == offsetInfo)
					return null;
				containingNode = offsetInfo.getContainingNode();
			}

			if (null == containingNode)
				return null;

			IIdentifierNode node = null;

			if (!(containingNode instanceof IMemberAccessExpressionNode)) {
				containingNode = containingNode
						.getAncestorOfType(IMemberAccessExpressionNode.class);
			}

			if (null != containingNode
					&& containingNode instanceof IMemberAccessExpressionNode) {
				node = (IIdentifierNode) ((IMemberAccessExpressionNode) containingNode)
						.getLeftOperandNode();
			}

			if (null != node) {
				// need imported.
				Set<IDefinition> definitions = SearchManager.getInstance()
						.getDefinitions(document, offset, node.getName());
				if (null != definitions && !definitions.isEmpty()) {
					return definitions;
				}
				// IImportTarget importTarget =
				// CMFactory.getImportTargetFactory()
				// .getImportTargetForQualifiedName(node.getName());
				// if
				// (CMFactory.getASIdentifierAnalyzer().isValidIdentifierName(
				// node.getName())) {
				// System.out.println(importTarget.getTargetName());
				// }
			}

			// if (containingNode instanceof IdentifierNode) {
			// containingNode = containingNode.getParent();
			// }

			// if (containingNode instanceof MemberAccessExpressionNode)
			// {
			// containingNode.getAncestorOfType(
			// }

			// if (null != definition) {
			// ((IASModel)
			// document).insertImport(definition.getQualifiedName(), 0);
			// }

		} finally {
			if (null != document) {
				document = null;
				documentProvider.disconnect(file);
			}
		}
		return results;
	}

	List<CodeCompletionResult> calculateCompletion(IFile file, int offset)
			throws Exception {
		final List<CodeCompletionResult> results = Lists.newArrayList();

		ASCorePlugin corePlugin = ASCorePlugin.getDefault();
		IDocumentProvider documentProvider = corePlugin.getDocumentProvider();
		// connects the file.
		documentProvider.connect(file);
		// retrieves the actionscript document.
		IFlexDocument document = (IFlexDocument) documentProvider
				.getDocument(file);
		try {
			if (null != document) {
                
				IDefinition definition = ASOffsetInformation.getDefinition(((IASModel)document).getBaseFileNode(), offset);

				ITextViewer textViewer = new DummyTextViewer(document, 0, 0);
				ActionScriptCompletionProcessor processor = new ActionScriptCompletionProcessor();
				// IContextInformation[] computeContextInformation = processor
				// .computeContextInformation(textViewer, offset);
				// processor.getProposalState()
				if (processor.hasCompletionProposals(textViewer, offset)) {
					ICompletionProposal[] proposals = processor
							.computeCompletionProposals(textViewer, offset);
					for (ICompletionProposal proposal : proposals) {
						String completion = null;
						String menu = proposal.getDisplayString();
						@SuppressWarnings("unused")
                        String info;
                        try {
    						info = proposal.getAdditionalProposalInfo();
                        } catch (Exception e) {
                        }
						String abbreviation = null;
						String type = "x";
						String replacementString = null;

						if (proposal instanceof ActionScriptCompletionProposal) {
							ActionScriptCompletionProposal lazy = (ActionScriptCompletionProposal) proposal;
							abbreviation = lazy.getName();
							menu = lazy.getStyledDisplayString().toString();
							replacementString = lazy.getReplacementString();
						} else if (proposal instanceof FlexCompletionProposal) {
							FlexCompletionProposal lazy = (FlexCompletionProposal) proposal;
							abbreviation = lazy.getName();
							menu = lazy.getDisplayString();
							replacementString = lazy.getReplacementString();
						}

						Field fReplacementOffset = FlexCompletionProposal.class
								.getDeclaredField("fReplacementOffset");
						boolean accessibleFlag = fReplacementOffset
								.isAccessible();
						fReplacementOffset.setAccessible(true);
						int replacementOffset = fReplacementOffset
								.getInt(proposal);
						fReplacementOffset.setAccessible(accessibleFlag);

						int replacementLength = offset - replacementOffset;

						completion = replacementString
								.substring(replacementLength);

						// results.add(new CodeCompletionResult(completion,
						// abbreviation, menu, Strings.isNullOrEmpty(info) ?
						// menu : info, type));
						results.add(new CodeCompletionResult(completion,
								abbreviation, menu, menu, type));
					}
				}

			}
		} finally {
			if (null != document) {
				document = null;
				documentProvider.disconnect(file);
			}
		}
		return results;
	}

}

// vim:ft=java
