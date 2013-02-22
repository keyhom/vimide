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
package org.vimide.eclipse.jdt.util;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.vimide.eclipse.core.util.EclipseProjectUtil;

import com.google.common.collect.Lists;

/**
 * The utilities for eclipse JDT plugin.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class EclipseJdtUtil {

    /**
     * Determines and return the line delimiter for the specific Java element.
     * 
     * @param elem the specific java element.
     * @return the line delimiter.
     */
    public static String getLineDelimiter(IJavaElement elem) {
        IOpenable openable = elem.getOpenable();
        if (openable instanceof ITypeRoot) {
            try {
                return openable.findRecommendedLineSeparator();
            } catch (JavaModelException ignore) {
                // use project settings.
            }
        }
        IProject project = elem.getJavaProject().getProject();
        return EclipseProjectUtil.getProjectLineDelimiter(project);
    }

    public static IProblem[] getProblems(ICompilationUnit src) throws Exception {
        return getProblems(src, null);
    }

    @SuppressWarnings("deprecation")
    public static IProblem[] getProblems(ICompilationUnit src, final int[] ids)
            throws Exception {
        if (null != src && src.exists()) {
            ICompilationUnit workingCopy = src.getWorkingCopy(null);
            final List<IProblem> problems = Lists.newArrayList();
            IProblemRequestor requestor = new IProblemRequestor() {

                @Override
                public boolean isActive() {
                    return true;
                }

                @Override
                public void endReporting() {
                }

                @Override
                public void beginReporting() {
                }

                @Override
                public void acceptProblem(IProblem problem) {
                    if (ids != null) {
                        for (int id : ids) {
                            if (problem.getID() == id)
                                problems.add(problem);
                        }
                    } else {
                        problems.add(problem);
                    }
                }
            };
            try {
                workingCopy.discardWorkingCopy();
                workingCopy.becomeWorkingCopy(requestor, null);
            } finally {
                workingCopy.discardWorkingCopy();
            }

            return problems.toArray(new IProblem[problems.size()]);
        }
        return null;
    }

    public static IProblem[] getProblems(IFile ifile) throws Exception {
        return getProblems(ifile, null);
    }

    public static IProblem[] getProblems(IFile iFile, int[] ids)
            throws Exception {
        if (null != iFile && iFile.exists()) {
            return getProblems(JavaCore.createCompilationUnitFrom(iFile), ids);
        }
        return null;
    }

    /**
     * Gets the primary element ( compilation unit or class file ) for the
     * supplied element.
     * 
     * @param element the element.
     * @return the primary element.
     */
    public static IJavaElement getPrimaryElement(IJavaElement element) {
        IJavaElement parent = element;

        while (parent.getElementType() != IJavaElement.COMPILATION_UNIT
                && parent.getElementType() != IJavaElement.CLASS_FILE) {
            parent = parent.getParent();
        }

        return parent;
    }
}
