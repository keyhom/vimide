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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;
import org.vimide.eclipse.core.util.EclipseResourceUtil;
import org.vimide.eclipse.jdt.util.EclipseJdtUtil;

import com.google.common.collect.Lists;

/**
 * Requests to update the supplied source element.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/javaUpdateSrcFile")
public class JavaUpdateSrcServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {
        final IProject project = getProject(req);
        final File file = getFile(req);
        final boolean validate = req.getIntParameter("validate", 0) == 1 ? true
                : false;
        final boolean build = req.getIntParameter("build", 0) == 1 ? true
                : false;

        final IPath relativePath = new Path(file.getAbsolutePath())
                .makeRelativeTo(project.getLocation());
        final IFile iFile = project.getFile(relativePath);

        final NullProgressMonitor monitor = new NullProgressMonitor();

        try {
            // refresh the file to clean the dirty.
            iFile.refreshLocal(IResource.DEPTH_INFINITE, monitor);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        if (validate) {
            List<Map<String, Object>> results = Lists.newArrayList();

            ICompilationUnit src = JavaCore.createCompilationUnitFrom(iFile); // src.getBuffer()

            try {
                IProblem[] problems = EclipseJdtUtil.getProblems(src);
                String fileName = src.getResource().getLocation().toOSString()
                        .replace('\\', '/');
                for (IProblem problem : problems) {
                    int[] pos = new FileObject(file).getLineColumn(problem
                            .getSourceStart());
                    Map<String, Object> m = EclipseResourceUtil
                            .wrapProblemAsMap(problem.getMessage(),
                                    problem.getSourceStart(),
                                    problem.getSourceEnd(), fileName,
                                    problem.getSourceLineNumber(), pos[1],
                                    problem.isError() ? 2 : 1);
                    if (null != m)
                        results.add(m);
                }
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }

            if (build) {
                try {
                    project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
                            null);
                } catch (CoreException ignore) {
                    ignore.printStackTrace();
                }
            }

            resp.writeAsJson(results);
        }
    }

}
