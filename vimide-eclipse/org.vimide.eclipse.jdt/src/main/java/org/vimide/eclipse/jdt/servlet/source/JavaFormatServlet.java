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
package org.vimide.eclipse.jdt.servlet.source;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;
import org.vimide.eclipse.jdt.util.EclipseJdtUtil;

/**
 * Requests to format the specific java source.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/java_src_format")
public class JavaFormatServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(JavaFormatServlet.class);

    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {
        final IProject project = getProject(req);
        IJavaProject javaProject = null;

        // Validate the project.
        if (null != project && project.exists()) {
            javaProject = JavaCore.create(project);
        }

        if (null == javaProject) {
            resp.sendError(403);
            return;
        }

        // Validate the file.
        final File file = getFile(req);

        if (null == file || !file.exists()) {
            resp.sendError(403);
            return;
        }

        // take default formatting options.
        Map<String, String> options = DefaultCodeFormatterConstants
                .getEclipseDefaultSettings();
        // fill with the project specific options.
        options.putAll(javaProject.getOptions(true));
        // fill with default options.
        fillDefaultOptions(options);

        // instantiate the default code formatter with the given options.
        final CodeFormatter codeFormatter = ToolFactory
                .createCodeFormatter(options);

        // retrieves the source to format.
        String source = null;
        ICompilationUnit src = null;
        try {
            // retrieves the file in project.
            IPath path = new Path(file.getPath()).makeRelativeTo(project
                    .getLocation());
            IFile iFile = project.getFile(path);
            if (iFile.exists()) {
                iFile.refreshLocal(IResource.DEPTH_INFINITE, null); // iFile.getPersistentProperties()
                src = JavaCore.createCompilationUnitFrom(iFile);
                if (null != src && src.exists())
                    source = src.getBuffer().getContents();
            }
        } catch (JavaModelException e) {
            LOGGER.error("Error caught: ", e.getMessage(), e);
        } catch (CoreException e) {
            LOGGER.error("Error caught: ", e.getMessage(), e);
        }

        if (null != source) {

            int startPosition = 0;
            int contentLength = 0;

            int bByteOffset = req.getIntParameter("hoffset");
            int eByteOffset = req.getIntParameter("toffset");

            if (bByteOffset >= 0 && eByteOffset > 0) {
                // process the offset.
                byte[] byteSource = source.getBytes();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                outStream.write(byteSource, 0, bByteOffset);

                String sourcePrefix = outStream.toString();

                outStream = new ByteArrayOutputStream();
                outStream.write(byteSource, bByteOffset, eByteOffset
                        - bByteOffset > 0 ? eByteOffset - bByteOffset : 0);
                String sourceRoot = outStream.toString();

                int bCharOffset = sourcePrefix.length();
                int eCharOffset = bCharOffset + sourceRoot.length();
                int charLength = eCharOffset - bCharOffset;

                startPosition = bCharOffset;
                contentLength = charLength;
            } else {
                startPosition = 0;
                contentLength = source.length();
            }

            final TextEdit textEdit = codeFormatter.format(
                    CodeFormatter.K_COMPILATION_UNIT
                            | CodeFormatter.F_INCLUDE_COMMENTS, source,
                    startPosition, contentLength, 0,
                    EclipseJdtUtil.getLineDelimiter(src));

            IDocument document = new Document(source);

            try {
                textEdit.apply(document);
            } catch (MalformedTreeException e) {
                LOGGER.error("", e);
            } catch (BadLocationException e) {
                LOGGER.error("", e);
            }

            if (null != src && null != document) {
                try {
                    src.getBuffer().setContents(document.get());

                    if (src.isWorkingCopy()) {
                        src.commitWorkingCopy(false, null);
                    }

                    src.save(null, false);
                } catch (JavaModelException e) {
                    LOGGER.error("", e);
                }

                resp.writeAsJson(1);
                return;
            }
        }

        resp.writeAsJson(0);
    }

    private void fillDefaultOptions(Map<String, String> options) {
        if (null != options) {
            options.put(
                    DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS,
                    DefaultCodeFormatterConstants.TRUE);
            options.put(
                    DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_NEW_LINE_FOR_PARAMETER,
                    DefaultCodeFormatterConstants.FALSE);
            options.put(
                    DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT,
                    DefaultCodeFormatterConstants.TRUE);
            options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR,
                    "space");
            options.put(
                    DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH,
                    DefaultCodeFormatterConstants.TRUE);
            options.put(
                    DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_ENUM_CONSTANT,
                    "48");
            options.put(
                    DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_SUPERINTERFACES_IN_ENUM_DECLARATION,
                    "48");
            options.put(
                    DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
                    "49");
        }
    }
}
