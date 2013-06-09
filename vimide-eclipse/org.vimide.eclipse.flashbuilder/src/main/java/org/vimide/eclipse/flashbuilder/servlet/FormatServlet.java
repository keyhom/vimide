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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;
import org.vimide.eclipse.jface.text.DummyTextViewer;

import com.adobe.flexide.as.core.ASCorePlugin;
import com.adobe.flexide.as.core.format.ASAutoIndent;
import com.adobe.flexide.editorcore.document.IFlexDocument;

/**
 * Requests used to indent the source correctly.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/flexFormat")
public class FormatServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(FormatServlet.class
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

        try {
            format(file, hOffset, tOffset);
        } catch (Exception e) {
            log.error("", e);
        }

        resp.writeAsJson(1);
    }

    protected void format(IFile file, int startOffset, int endOffset)
            throws Exception {

        IFlexDocument doc = null;
        IDocumentPartitioner partitioner = null;
        DocumentRewriteSession session = null;

        ASCorePlugin corePlugin = ASCorePlugin.getDefault();
        IDocumentProvider documentProvider = corePlugin.getDocumentProvider();

        if (null == documentProvider)
            return;

        try {
            documentProvider.connect(file);
            doc = (IFlexDocument) documentProvider.getDocument(file);
            session = doc
                    .startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
            
            DummyTextViewer viewer = new DummyTextViewer(doc, startOffset,
                    endOffset);
            ITextSelection selection = (ITextSelection) viewer
                    .getSelectionProvider().getSelection();

            boolean isSingleLine = (selection.isEmpty())
                    || (selection.getLength() == 0);

            Position startPos = null;
            Position endPos = null;

            final ASAutoIndent autoIndent = new ASAutoIndent();
            autoIndent.initDebug();

            try {
                int startLine;
                int endLine;

                if (isSingleLine) {
                    startLine = doc.getLineOfOffset(selection.getOffset());
                    endLine = startLine;
                    startPos = new Position(selection.getOffset());
                    doc.addPosition(startPos);
                } else {
                    startLine = selection.getStartLine();
                    endLine = selection.getEndLine();
                    startPos = new Position(selection.getOffset());
                    doc.addPosition(startPos);
                    endPos = new Position(selection.getOffset()
                            + selection.getLength());
                    doc.addPosition(endPos);
                }

                if (endLine - startLine > 50) {
                    partitioner = doc.getDocumentPartitioner();
                    partitioner.disconnect();
                    doc.setDocumentPartitioner(null);
                }

                setIndentOption(autoIndent);
                autoIndent.correctIndentation(doc, startLine, endLine);
            } catch (final BadLocationException e) {
                log.error("", e.getLocalizedMessage(), e);

                if (null != partitioner) {
                    doc.setDocumentPartitioner(partitioner);
                    partitioner.connect(doc);
                }
                doc.stopRewriteSession(session);
            } finally {
                if (partitioner != null) {
                    doc.setDocumentPartitioner(partitioner);
                    partitioner.connect(doc);
                }
                doc.stopRewriteSession(session);

            }

            if (isSingleLine) {
                if (null != startPos) {
                    int offset = startPos.getOffset();
                    try {
                        IRegion lineInfo = doc
                                .getLineInformationOfOffset(offset);
                        String line = doc.get(lineInfo.getOffset(),
                                lineInfo.getLength());
                        if (line.trim().length() != 0)
                            viewer.setSelectedRange(offset, 0);
                        else
                            offset = lineInfo.getOffset()
                                    + lineInfo.getLength();
                    } catch (final BadLocationException e) {
                        log.error("", e.getLocalizedMessage(), e);
                    }

                    doc.removePosition(startPos);
                }
            } else if ((null != startPos) && (null != endPos)) {
                int len = endPos.getOffset() - startOffset;
                viewer.setSelectedRange(startOffset, len);
                doc.removePosition(startPos);
                doc.removePosition(endPos);
            }

        } finally {
            if (null != documentProvider) {
                if (null != doc && doc.isDirty()) {
                    documentProvider.saveDocument(null, file, doc, true);
                }
            }
        }
    }

    protected void setIndentOption(ASAutoIndent autoIndent) throws Exception {
        autoIndent.setAlignFunctionParameters(false);
        autoIndent.setIndentPackageContents(false);
        autoIndent.setIndentSwitchContents(true);
    }

}

// vim:ft=java
