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
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.complete.CodeCompletionResult;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;
import org.vimide.eclipse.jface.text.DummyTextViewer;

import com.adobe.flexbuilder.codemodel.common.CMFactory;
import com.adobe.flexbuilder.codemodel.common.IImportTarget;
import com.adobe.flexbuilder.codemodel.definitions.IDefinition;
import com.adobe.flexbuilder.codemodel.definitions.IDefinitionLink;
import com.adobe.flexbuilder.codemodel.internal.tree.IdentifierNode;
import com.adobe.flexbuilder.codemodel.internal.tree.MemberAccessExpressionNode;
import com.adobe.flexbuilder.codemodel.tree.ASOffsetInformation;
import com.adobe.flexbuilder.codemodel.tree.IASNode;
import com.adobe.flexbuilder.codemodel.tree.IMemberAccessExpressionNode;
import com.adobe.flexide.as.core.ASCorePlugin;
import com.adobe.flexide.as.core.IASDataProvider;
import com.adobe.flexide.as.core.contentassist.ActionScriptCompletionProcessor;
import com.adobe.flexide.as.core.contentassist.ActionScriptCompletionProposal;
import com.adobe.flexide.editorcore.contentassist.FlexCompletionProposal;
import com.adobe.flexide.editorcore.document.IFlexDocument;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Requests used to calc the code completions.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@SuppressWarnings("restriction")
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

        // String layout = req.getParameter("layout");

        Map<String, Object> results = Maps.newHashMap();

        try {
            results.put("error", "");
            results.put("imports", "");
            results.put("completions",
                    calculateCompletion(project, file, offset));

        } catch (final Exception e) {
            e.printStackTrace();
        }

        resp.writeAsJson(results);
    }

    @SuppressWarnings("restriction")
    Object calculateCompletion(IProject project, File file, int offset)
            throws Exception {
        final List<CodeCompletionResult> results = Lists.newArrayList();
        IFile ifile = getProjectFile(project, file.getAbsolutePath());

        ASCorePlugin corePlugin = ASCorePlugin.getDefault();
        IDocumentProvider documentProvider = corePlugin.getDocumentProvider();
        // connects the file.
        documentProvider.connect(ifile);
        // retrieves the actionscript document.
        IFlexDocument document = (IFlexDocument) documentProvider
                .getDocument(ifile);
        IASDataProvider dataProvider = null;
        try {
            if (null != document) {
                ASOffsetInformation offsetInfo = null;
                IASNode containingNode = null;

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

                IDefinition definition = null;
                IdentifierNode node = null;

                if (!(containingNode instanceof MemberAccessExpressionNode)) {
                    containingNode = containingNode
                            .getAncestorOfType(IMemberAccessExpressionNode.class);
                }

                if (null != containingNode
                        && containingNode instanceof MemberAccessExpressionNode) {
                    node = (IdentifierNode) ((MemberAccessExpressionNode) containingNode)
                            .getLeft();
                }

                if (null != node) {
                    // need imported.
                    IImportTarget importTarget = CMFactory.getImportTargetFactory()
                            .getImportTargetForQualifiedName(node.getName());
                    if (CMFactory.getASIdentifierAnalyzer().isValidIdentifierName(node.getName())) {
                        // System.out.println(offsetInfo.getDefinition()); 
                        // offsetInfo.getDefinition(((BaseASModel)document).getBaseFileNode(), 1746)
                        System.out.println(node.getAdapter(IDefinitionLink.class));
                    }
                }

                // if (containingNode instanceof IdentifierNode) {
                // containingNode = containingNode.getParent();
                // }

                // if (containingNode instanceof MemberAccessExpressionNode) {
                // containingNode.getAncestorOfType(
                // }

                // if (null != definition) {
                // ((IASModel)
                // document).insertImport(definition.getQualifiedName(), 0);
                // }

                ITextViewer textViewer = new DummyTextViewer(document, 0, 0);
                ActionScriptCompletionProcessor processor = new ActionScriptCompletionProcessor();
                IContextInformation[] computeContextInformation = processor
                        .computeContextInformation(textViewer, offset);
                // processor.getProposalState()
                if (processor.hasCompletionProposals(textViewer, offset)) {
                    ICompletionProposal[] proposals = processor
                            .computeCompletionProposals(textViewer, offset);
                    for (ICompletionProposal proposal : proposals) {
                        String completion = null;
                        String menu = proposal.getDisplayString();
                        @SuppressWarnings("unused")
                        String info = proposal.getAdditionalProposalInfo();
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

                        Field fReplacementLength = FlexCompletionProposal.class
                                .getDeclaredField("fReplacementLength");
                        boolean accessibleFlag = fReplacementLength
                                .isAccessible();
                        fReplacementLength.setAccessible(true);
                        int replacementLength = fReplacementLength
                                .getInt(proposal);
                        fReplacementLength.setAccessible(accessibleFlag);
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
                documentProvider.disconnect(ifile);
            }
        }
        return results;
    }
}

// vim:ft=java
