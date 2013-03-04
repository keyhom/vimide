/**
 * Copyright (c) 2012 keyhom.c@gmail.com.
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

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.vimide.core.util.Os;
import org.vimide.core.util.Position;
import org.vimide.eclipse.jdt.search.SearchRequestor;
import org.vimide.eclipse.jdt.util.EclipseJdtUtil;

import com.google.common.base.Strings;

/**
 * Represents a service for searching.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@SuppressWarnings("restriction")
public class JavaSearchService extends JavaBaseService {

    /**
     * Constants of "Project Scope".
     */
    private static final String SCOPE_PROJECT = "project";

    /**
     * Consts Pattern of "Inner class".
     */
    private static final Pattern INNER_CLASS = Pattern
            .compile("(.*?)(\\w+\\$)(\\w.*)");

    /**
     * A singleton holder for {@link JavaSearchService}.
     */
    private static class SingletonHolder {
        static final JavaSearchService instance = new JavaSearchService();
    }

    /**
     * Gets the singleton instance.
     * 
     * @return singleton.
     */
    public static JavaSearchService getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Creates a JavaSearchService instance by private.
     */
    private JavaSearchService() {
        super();
    }

    /**
     * Collects the matches by the supplied condition arguments.
     * 
     * @param src the source file.
     * @param offset the offset.
     * @param length the length.
     * @param caseSensitive tell whether case sensitive.
     * @param type the type.
     * @param scope the scope.
     * @param pat the searching pattern.
     * @return the match result by search.
     * @throws Exception
     */
    public List<SearchMatch> collectMatches(ICompilationUnit src, int offset,
            int length, boolean caseSensitive, int type, String scope,
            String pat) throws Exception {

        if (null == src)
            return null;

        int context = -1;
        SearchPattern pattern = null;
        IJavaProject javaProject = src.getJavaProject();

        // element search.
        if (offset > 0 && length > 0) {
            IJavaElement element = null;
            IJavaElement[] elements = src.codeSelect(offset, length);
            if (null != elements && elements.length > 0) {
                element = elements[0];
            }

            if (null != element) {
                // usr requested a contextual search.
                if (-1 == context) {
                    context = getElementContextualContext(element);
                }
                pattern = SearchPattern.createPattern(element, context);
            }
        } else if (!Strings.isNullOrEmpty(pat)) {
            if (-1 == context) {
                context = IJavaSearchConstants.DECLARATIONS;
            }

            int matchType = SearchPattern.R_EXACT_MATCH;

            // wild card character supplied, use pattern matching.
            if (pat.indexOf('*') != -1 || pat.indexOf('?') != -1) {
                matchType = SearchPattern.R_PATTERN_MATCH;
            } else if (pat.equals(pat.toUpperCase())) {
                matchType |= SearchPattern.R_CAMELCASE_MATCH;
            }

            if (caseSensitive) {
                matchType |= SearchPattern.R_CASE_SENSITIVE;
            }

            // hack for inner class.
            Matcher matcher = INNER_CLASS.matcher(pat);
            if (matcher.matches()) {
                // pattern search doesn't support org.test.Type$Inner or
                // org.test.Type.Inner, so convert it to org.test.*Inner, then
                // filter the results.
                pattern = SearchPattern
                        .createPattern(matcher.replaceFirst("$1*$3"), type,
                                context, matchType);
                Pattern toMatch = Pattern.compile(pat.replace(".", "\\.")
                        .replace("$", "\\$").replace("(", "\\(")
                        .replace(")", "\\)").replace("*", ".*")
                        .replace("?", "."));

                List<SearchMatch> matches = search(pattern,
                        getScope(scope, javaProject));
                Iterator<SearchMatch> iterator = matches.iterator();
                while (iterator.hasNext()) {
                    SearchMatch match = iterator.next();
                    String name = getFullyQualifiedName(
                            (IJavaElement) match.getElement())
                            .replace("#", ".");
                    if (!toMatch.matcher(name).matches()) {
                        iterator.remove();
                    }
                }

                return matches;
            }

            pattern = SearchPattern
                    .createPattern(pat, type, context, matchType);
        } else { // bad search request.
            throw new IllegalArgumentException("bad search request");
        }

        List<SearchMatch> matches = search(pattern,
                getScope(scope, javaProject));
        return matches;
    }

    /**
     * Determines the appropriate context to used base on the elements context.
     * 
     * @param element the java element.
     * @return context value
     */
    protected int getElementContextualContext(IJavaElement element) {
        Class<?> theClass = element.getClass();

        // type / field / method declaration.
        if (theClass.equals(org.eclipse.jdt.internal.core.SourceType.class)
                || theClass
                        .equals(org.eclipse.jdt.internal.core.SourceField.class)
                || theClass
                        .equals(org.eclipse.jdt.internal.core.SourceMethod.class)) {
            return IJavaSearchConstants.ALL_OCCURRENCES;
        }
        return IJavaSearchConstants.DECLARATIONS;
    }

    /**
     * Gets the search scope to use.
     * 
     * @param scope the string name of scope.
     * @param javaProject the current java project.
     * @return the java search scope element.
     * @throws Exception
     */
    protected IJavaSearchScope getScope(String scope, IJavaProject javaProject)
            throws Exception {
        if (null != javaProject && SCOPE_PROJECT.equals(scope)) {
            return SearchEngine
                    .createJavaSearchScope(new IJavaElement[] { javaProject });
        }
        return SearchEngine.createWorkspaceScope();
    }

    /**
     * Search execution.
     * 
     * @param pattern the search pattern.
     * @param scope the scope of ths search (file, project, all, etc)
     * @return list of matches.
     * @throws Exception
     */
    protected List<SearchMatch> search(SearchPattern pattern,
            IJavaSearchScope scope) throws Exception {
        SearchRequestor requestor = new SearchRequestor();

        if (null != pattern) {
            SearchEngine engine = new SearchEngine();
            SearchParticipant[] participants = new SearchParticipant[] { SearchEngine
                    .getDefaultSearchParticipant() };
            engine.search(pattern, participants, scope, requestor, null);
        }
        return requestor.getMatches();
    }

    /**
     * Creates a position from the supplied SearchMatch object.
     * 
     * @param project the project searching from.
     * @param match the SearchMatch object.
     * @return the position.
     * @throws Exception
     */
    public Position createPosition(IProject project, SearchMatch match)
            throws Exception {
        IJavaElement element = (IJavaElement) match.getElement();
        IJavaElement parent = EclipseJdtUtil.getPrimaryElement(element);

        String file = null;
        String elementName = getFullyQualifiedName(parent);

        if (IJavaElement.CLASS_FILE == parent.getElementType()) {
            IResource resource = parent.getResource();
            // occurs with a referenced project as a lib with no source and
            // class files that are not archived in that project.
            if (null != resource && IResource.FILE == resource.getType()
                    && !isJarArchive(resource.getLocation())) {
                file = resource.getLocation().toOSString();
            } else {
                IPath path = null;
                IPackageFragmentRoot root = (IPackageFragmentRoot) parent
                        .getParent().getParent();
                resource = root.getResource();
                if (null != resource) {
                    if (IResource.PROJECT == resource.getType()) {
                        path = getIPath((IProject) resource);
                    } else {
                        path = resource.getLocation();
                    }
                } else {
                    path = root.getPath();
                }

                String classFile = elementName.replace('.', File.separatorChar);

                if (isJarArchive(path)) {
                    file = "jar:file://" + path.toOSString() + "!/" + classFile
                            + ".class";
                } else {
                    file = path.toOSString() + "/" + classFile + ".class";
                }

                // android injects its jdk classes, so filter those out if
                // the project doesn't have the android nature.
                // TODO: filter by the android nature.

                // if a source path attachment exists, use it.
                IPath srcPath = root.getSourceAttachmentPath();
                if (null != srcPath) {
                    String rootPath;
                    IProject elementProject = root.getJavaProject()
                            .getProject();

                    // determines if src path is project relative or file
                    // system absolute.
                    if (srcPath.isAbsolute()
                            && elementProject.getName().equals(
                                    srcPath.segment(0))) {
                        rootPath = getFilePath(elementProject,
                                srcPath.toString());
                    } else {
                        rootPath = srcPath.toOSString();
                    }

                    String srcFile = toUrl(rootPath + File.separator
                            + classFile + ".java");

                    // see if source file exists at source file.
                    FileSystemManager fsManager = VFS.getManager();
                    FileObject fileObject = fsManager.resolveFile(srcFile);
                    if (fileObject.exists()) {
                        file = srcFile;
                    } else if (Os.isFamily(Os.FAMILY_MAC)) {
                        srcFile = toUrl(rootPath + File.separator + "src"
                                + File.separator + classFile + ".java");
                        fileObject = fsManager.resolveFile(srcFile);
                        if (fileObject.exists()) {
                            file = srcFile;
                        }
                    }
                }
            }
        } else {
            IPath location = match.getResource().getLocation();
            file = null != location ? location.toOSString() : null;
        }

        elementName = getFullyQualifiedName(element);
        return Position.fromOffset(file.replace('\\', '/'), elementName,
                match.getOffset(), match.getLength());
    }
}
