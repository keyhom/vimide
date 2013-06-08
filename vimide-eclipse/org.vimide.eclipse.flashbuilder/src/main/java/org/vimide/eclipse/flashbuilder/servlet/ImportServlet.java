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
import java.util.Arrays;
import java.util.List;

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

import com.adobe.flexbuilder.codemodel.common.CMFactory;
import com.adobe.flexbuilder.codemodel.common.IImportTarget;
import com.adobe.flexbuilder.codemodel.definitions.IClass;
import com.adobe.flexbuilder.codemodel.definitions.IDefinition;
import com.adobe.flexbuilder.codemodel.definitions.IInterface;
import com.adobe.flexbuilder.codemodel.indices.IClassNameIndex;
import com.adobe.flexbuilder.codemodel.indices.IInterfaceNameIndex;
import com.adobe.flexbuilder.codemodel.internal.tree.IdentifierNode;
import com.adobe.flexbuilder.codemodel.project.IProject;
import com.adobe.flexbuilder.codemodel.tree.ASOffsetInformation;
import com.adobe.flexbuilder.codemodel.tree.IASNode;
import com.adobe.flexbuilder.codemodel.tree.IExpressionNode;
import com.adobe.flexbuilder.codemodel.tree.IIdentifierNode;
import com.adobe.flexbuilder.codemodel.tree.IImportNode;
import com.adobe.flexbuilder.codemodel.tree.IScopedNode;
import com.adobe.flexide.as.core.ASCorePlugin;
import com.adobe.flexide.as.core.IASDataProvider;
import com.adobe.flexide.as.core.document.IASModel;
import com.adobe.flexide.editorcore.document.IFlexDocument;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Requests used to import the specific type.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/flexImport")
@SuppressWarnings("restriction")
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

	/**
	 * @private
	 */
	protected Object importType(IFile file, int offset, String typeName)
			throws Exception {
		// Determines whether the typeName suppling null was.
		ASCorePlugin corePlugin = ASCorePlugin.getDefault();
		IDocumentProvider documentProvider = corePlugin.getDocumentProvider();
		// connects the file.
		documentProvider.connect(file);
		// retrieves the actionscript document.
		IFlexDocument document = (IFlexDocument) documentProvider
				.getDocument(file);

		try {
			IASNode containingNode = null;
			ASOffsetInformation offsetInfo = null;
			IASDataProvider dataProvider = null;
			IdentifierNode node = null;

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

				if (containingNode instanceof IdentifierNode) {
					node = (IdentifierNode) containingNode;
				}

				if (null != node && Strings.isNullOrEmpty(typeName)) {
					typeName = node.getName();
				}
			}

			if (!Strings.isNullOrEmpty(typeName)) {
				String[] importName = null;
				synchronized (CMFactory.getLockObject()) {
					IExpressionNode[] names = ASOffsetInformation
							.getQualifiedNames(typeName, offset);
					if (names.length != 1) {
						return null;
					}

					if (!(names[0] instanceof IIdentifierNode)) {
						return null;
					}

					if (((IIdentifierNode) names[0]).getName().compareTo(
							typeName) != 0) {
						return null;
					}

					if ((typeName.indexOf(46) != -1)
							&& (typeName.indexOf(".<") == -1)) {
						importName = new String[] { typeName };
					} else {
						importName = determineImportForShortName(
								(IASModel) document, typeName, offset);
					}
				}

				if (null != importName && importName.length > 1) {
					return importName;
				} else {
                    int resultOffset = offset;
					if (null != importName && importName.length == 1) {
						((IASModel) document).insertImport(importName[0],
								offset);
						((IASModel) document).organizeImports(false);
                        resultOffset = offset + typeName.length() + 10;
					}
					return Position.fromOffset(file.getLocation().toOSString(),
							null, resultOffset, 0);
				}
			}
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

		return null;
	}

	/**
	 * 
	 * @param shortName
	 * @param offset
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected String[] determineImportForShortName(IASModel baseModel,
			String shortName, int offset) {
		String resultImport = null;
		synchronized (CMFactory.getLockObject()) {
			@SuppressWarnings("rawtypes")
			List imports = Lists.newArrayList();
			String currentPackageName = null;

			IProject project = CMFactory.getManager().getProjectForDocument(
					baseModel);
			ASOffsetInformation offsetInfo = new ASOffsetInformation(offset,
					offset, baseModel.getBaseFileNode());
			IASNode containingNode = offsetInfo.getContainingNode();
			if (null != containingNode) {
				currentPackageName = containingNode.getPackageName();
				if (!(containingNode instanceof IScopedNode)) {
					containingNode = containingNode
							.getAncestorOfType(IScopedNode.class);
				}

				if (null != containingNode) {
					((IScopedNode) containingNode).getAllImportNodes(imports);
				}
			}

			IClassNameIndex index = (IClassNameIndex) project
					.getIndex("className");
			IClass[] retClasses = index.getByShortName(shortName);
			IInterfaceNameIndex iIndex = (IInterfaceNameIndex) project
					.getIndex("interface");
			IInterface[] retInter = iIndex.getByShortName(shortName);

			@SuppressWarnings("rawtypes")
			List matchingTypesList = Lists.newArrayList();
			matchingTypesList.addAll(Arrays.asList(retClasses));
			matchingTypesList.addAll(Arrays.asList(retInter));
			IDefinition[] matchingTypes = (IDefinition[]) matchingTypesList
					.toArray(new IDefinition[matchingTypesList.size()]);
			String[] qualifiedNames = new String[matchingTypes.length];
			for (int i = 0; i < matchingTypes.length; i++) {
				if (!(matchingTypes[i].isImplicit())) {
					qualifiedNames[i] = matchingTypes[i].getQualifiedName();
				}
			}

			boolean haveMatchingImport = false;

			if ((currentPackageName != null)
					&& (!(currentPackageName.equals("")))) {
				IImportTarget importTarget = CMFactory.getImportTargetFactory()
						.getImportTargetForPackageName(currentPackageName);
				for (int i = 0; i < qualifiedNames.length
						&& !haveMatchingImport; i++) {
					String matchedName = importTarget
							.getQualifiedName(qualifiedNames[i]);
					if (null != matchedName
							&& matchedName.equals(qualifiedNames[i])) {
						haveMatchingImport = true;
						break;
					}
				}

				if (!haveMatchingImport) {
					for (int i = 0; i < qualifiedNames.length; i++) {
						haveMatchingImport = importExistsInPackageScope(
								baseModel, qualifiedNames[i],
								(IImportNode[]) imports
										.toArray(new IImportNode[0]), project);
						if (haveMatchingImport)
							break;
					}

					if (!haveMatchingImport && matchingTypes.length == 1) {
						resultImport = qualifiedNames[0];
					} else if (!haveMatchingImport && matchingTypes.length > 1) {
						String[] results = new String[matchingTypes.length];
						for (int i = 0; i < results.length; i++) {
							results[i] = matchingTypes[i].getQualifiedName();
						}
						return results;
					}
				}
			}
		}

		if (null != resultImport)
			return new String[] { resultImport };
		return null;
	}

	/**
	 * @private
	 */
	private boolean importExistsInPackageScope(IASModel baseModel,
			String qualifiedName, IImportNode[] imports, IProject project) {
		IClassNameIndex classIndex = null;
		IInterfaceNameIndex interfaceIndex = null;
		boolean doEliminateImplicitEvents = false;
		IFile file = baseModel.getIFile();
		if (null != file) {
			doEliminateImplicitEvents = file.getFileExtension().compareTo("as") != 0;
			if (doEliminateImplicitEvents) {
				if (null != project) {
					classIndex = (IClassNameIndex) project
							.getIndex("className");
					interfaceIndex = (IInterfaceNameIndex) project
							.getIndex("interface");
				} else {
					doEliminateImplicitEvents = false;
				}
			}
		}

		IImportTarget startTarget = CMFactory.getImportTargetFactory()
				.getImportTargetForQualifiedName("*");

		String matchedName = startTarget.getQualifiedName(qualifiedName);
		if (null != matchedName && matchedName.equals(qualifiedName)) {
			return true;
		}

		if (imports.length > 0) {
			for (int i = 0; i < imports.length; i++) {
				IImportNode curImportNode = imports[i];

				if (curImportNode.getImportKind() == IImportNode.ImportKind.MXML_NAMESPACE_IMPORT) {
					continue;
				}

				IImportTarget importTarget = CMFactory.getImportTargetFactory()
						.getImportTargetForQualifiedName(
								curImportNode.getImportName());
				matchedName = importTarget.getQualifiedName(qualifiedName);
				if (null == matchedName
						|| matchedName.compareTo(qualifiedName) != 0)
					continue;

				if (doEliminateImplicitEvents) {
					if (curImportNode.getImportKind() == IImportNode.ImportKind.IMPLICIT_IMPORT) {
						if (matchedName.startsWith("flash.events.")) {
							String[] chunks = matchedName.split("\\.");
							if (null != chunks && chunks.length == 3) {
								return true;
							}
						}

						if (null != classIndex) {
							IClass clazz = classIndex
									.getByQualifiedName(matchedName);
							if (null != clazz)
								return !(clazz
										.isInstanceOf("flash.events.Event"));
						}

						if (null == interfaceIndex)
							continue;

						IInterface name = interfaceIndex
								.getByQualifiedName(qualifiedName);
						if (null != name)
							return !(name.isInstanceOf("flash.events.Event"));
					} else {
						return true;
					}
				} else
					return true;
			}
		}

		return false;
	}
}

// vim:ft=java
