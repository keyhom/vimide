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
package org.vimide.eclipse.jdt;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.eclipse.jdt.service.JavaSourceService;

/**
 * The facade for the java source functions.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public final class JavaSourceFacade {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(JavaSourceFacade.class.getName());

    /**
     * Creates a private JavaSourceFacade instance.
     */
    private JavaSourceFacade() {
        super();
    }

    /**
     * Formats the java source elements.
     * 
     * @param project the java project.
     * @param file the source file.
     * @param bOffset the begin source offset.
     * @param eOffset the end source offset.
     * @throws CoreException
     */
    public static boolean format(IJavaProject project, File file, int bOffset,
            int eOffset) throws CoreException {
        if (null == project || !project.exists()) {
            throw new CoreException(new Status(IStatus.ERROR,
                    VimideJdtPlugin.PLUGIN_ID, "Illegal project."));
        }

        if (null == file || !file.exists()) {
            throw new CoreException(new Status(IStatus.ERROR,
                    VimideJdtPlugin.PLUGIN_ID, "Illegal file."));
        }

        if (0 > bOffset || 0 == eOffset) {
            throw new CoreException(new Status(IStatus.ERROR,
                    VimideJdtPlugin.PLUGIN_ID, "Illegal offset."));
        }

        final JavaSourceService service = JavaSourceService.getInstance();
        ICompilationUnit src = service.getCompilationUnit(project.getProject(),
                file);

        return format(src, bOffset, eOffset);
    }

    /**
     * Makes a format operation with the supplied source by the begin offset and
     * end offset.
     * 
     * @param src the source file.
     * @param bOffset the begin offset.
     * @param eOffset the end offset.
     * @return true if format the source complete successfully, false otherwise.
     * @throws CoreException
     */
    public static boolean format(ICompilationUnit src, int bOffset, int eOffset)
            throws CoreException {
        final JavaSourceService service = JavaSourceService.getInstance();
        final Map<String, String> options = service.getOptions(src
                .getJavaProject());

        try {
            IDocument document = service.format(src, options, bOffset, eOffset);

            if (null != document) {
                service.save(src, document);
            }
            return true;
        } catch (final CoreException e) {
            LOGGER.error("", e);
        }

        return false;
    }

}
