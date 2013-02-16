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
package org.vimide.eclipse.jdt.service;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
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
import org.vimide.eclipse.jdt.VimideJdtPlugin;
import org.vimide.eclipse.jdt.util.EclipseJdtUtil;

import com.google.common.collect.Maps;

/**
 * The service implementation for the java source functions.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class JavaSourceService extends JavaBaseService {

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(JavaSourceService.class.getName());

    /**
     * Lazy singleton holder.
     * 
     * @author keyhom (keyhom.c@gmail.com)
     */
    private static class SingletonHolder {
        static final JavaSourceService instance = new JavaSourceService();
    }

    /**
     * Gets the singelton instance.
     * 
     * @return singleton.
     */
    public static JavaSourceService getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Gets the formatting options.
     * 
     * @return formatting options as map.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getOptions(final IJavaProject project) {
        if (null == project) {
            return null;
        }

        // take default formatting options.
        final Map<String, String> options = DefaultCodeFormatterConstants
                .getEclipseDefaultSettings();
        // fill with the project specific options.
        options.putAll(project.getOptions(true));
        // fill with the default options.
        options.putAll(getVimideDefaultOptions());

        return options;
    }

    protected Map<String, String> getVimideDefaultOptions() {
        final Map<String, String> options = Maps.newHashMap();
        options.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS,
                DefaultCodeFormatterConstants.TRUE);
        options.put(
                DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_NEW_LINE_FOR_PARAMETER,
                DefaultCodeFormatterConstants.FALSE);
        options.put(
                DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT,
                DefaultCodeFormatterConstants.TRUE);
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, "space");
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
        return options;
    }

    /**
     * Formats the supplied source with the specific formatter options.
     * 
     * @param src the source to format.
     * @param options the options to format.
     * @param bOffset the begin offset of the source.
     * @param eOffset the end offset of the source.
     * @return the document by being formatted.
     * @throws CoreException
     */
    public IDocument format(ICompilationUnit src, Map<String, String> options,
            int bOffset, int eOffset) throws CoreException {
        if (null == src)
            throw new CoreException(new Status(IStatus.ERROR,
                    VimideJdtPlugin.PLUGIN_ID, "Illegal src."));

        try {
            final String source = src.getBuffer().getContents();
            IDocument document = null;
            if (null != source) {
                document = new Document(source);
                int bByteOffset = bOffset;
                int eByteOffset = eOffset;
                int startPosition = 0;
                int contentLength = 0;

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

                final CodeFormatter codeFormatter = ToolFactory
                        .createCodeFormatter(options);

                final TextEdit textEdit = codeFormatter.format(
                        CodeFormatter.K_COMPILATION_UNIT
                                | CodeFormatter.F_INCLUDE_COMMENTS, source,
                        startPosition, contentLength, 0,
                        EclipseJdtUtil.getLineDelimiter(src));

                try {
                    textEdit.apply(document);
                } catch (MalformedTreeException e) {
                    LOGGER.error("", e);
                } catch (BadLocationException e) {
                    LOGGER.error("", e);
                }
            }
            return document;
        } catch (JavaModelException e) {
            LOGGER.error("", e);
        }
        return null;
    }

    /**
     * Saves the source by the supplied document.
     * 
     * @param src the source by compilation unit.
     * @param document the document to save.
     * @return true if save successfully, false otherwise.
     */
    public boolean save(ICompilationUnit src, IDocument document) {
        if (null != src && null != document) {
            try {
                src.getBuffer().setContents(document.get());
                if (src.isWorkingCopy()) {
                    src.commitWorkingCopy(false, null);
                }

                src.save(null, false);
                return true;
            } catch (final JavaModelException e) {
                LOGGER.error("", e);
            }
        }
        return false;
    }

    public void generateElementComment(ICompilationUnit src, int offset)
            throws JavaModelException {
        if (null != src) {
            int offsetLimit = src.getBuffer().getContents().getBytes().length;
            if (offset >= 0 && offset <= offsetLimit) {
                IJavaElement element = src.getElementAt(offset);

                if (null != element)
                    generateElementComment(src, element);
            }
        }
    }

    public void generateElementComment(ICompilationUnit src,
            IJavaElement element) {
        
    }

}
