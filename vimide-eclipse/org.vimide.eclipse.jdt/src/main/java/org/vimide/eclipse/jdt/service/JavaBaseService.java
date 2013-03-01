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

import java.io.File;
import java.util.Enumeration;

import javax.naming.CompositeName;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;

/**
 * The basic service for the java functionally service.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class JavaBaseService {

    public static final String JAR_PREFIX = "jar://";
    public static final String ZIP_PREFIX = "zip://";
    public static final String JAR_EXT = ".jar";
    public static final String ZIP_EXT = ".zip";

    /**
     * Gets the compilation unit by the supplied project and file.
     * 
     * @param project the specific project.
     * @param file the specific file belongs to the project.
     * @return the compilation unit element.
     */
    public ICompilationUnit getCompilationUnit(IProject project, File file) {
        if (null != project && null != file) {
            final IPath path = new Path(file.getPath()).makeRelativeTo(project
                    .getLocation());
            if (null != path)
                return getCompilationUnit(project, path);
        }
        return null;
    }

    /**
     * Gets the compilation unit by the supplied project and path.
     * 
     * @param project the specific project.
     * @param path the path of the specific file.
     * @return the compilation unit element.
     */
    public ICompilationUnit getCompilationUnit(IProject project, IPath path) {
        if (null != project && null != path) {
            IFile file = project.getFile(path);
            if (null != file && file.exists()) {
                // refresh locally at first.
                try {
                    file.refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (CoreException e) {
                }
                return JavaCore.createCompilationUnitFrom(file);
            }
        }
        return null;
    }

    /**
     * Gets the fully qualified name of the supplied java element.
     * <p/>
     * NOTE: for easy of determining fields and method segments, they are
     * appended with a javadoc style '#' instead of the normal '.'.
     * 
     * @param element the java element.
     * @return the fully qualified name.
     */
    public String getFullyQualifiedName(IJavaElement element) {
        IJavaElement parent = element;
        while (IJavaElement.COMPILATION_UNIT != parent.getElementType()
                && IJavaElement.CLASS_FILE != parent.getElementType()) {
            parent = parent.getParent();
        }

        String fileName = null;
        // Fetch the file name.
        IPath p = Path.fromOSString(element.getElementName());
        if (p.hasTrailingSeparator()) {
            fileName = StringUtils.EMPTY;
        } else {
            fileName = p.removeFileExtension().segment(p.segmentCount() - 1);
        }

        StringBuilder elementName = new StringBuilder();
        elementName.append(parent.getParent().getElementName());
        elementName.append('.');
        elementName.append(fileName);

        switch (element.getElementType()) {
            case IJavaElement.FIELD:
                IField field = (IField) element;
                elementName.append('#').append(field.getElementName());
                break;
            case IJavaElement.METHOD:
                IMethod method = (IMethod) element;
                elementName.append('#').append(method.getElementName())
                        .append('(');
                String[] parameters = method.getParameterTypes();
                for (int i = 0; i < parameters.length; i++) {
                    if (0 != i)
                        elementName.append(", ");
                    elementName.append(Signature.toString(parameters[i])
                            .replace('/', '.'));
                }
                elementName.append(')');
                break;
        }

        return elementName.toString();
    }

    /**
     * Gets a project by name.
     * 
     * @param name the name of the project.
     * @return the project which may or may not exist.
     * @throws Exception
     */
    public IProject getProject(String name) throws Exception {
        return getProject(name, false);
    }

    /**
     * Gets a project by name.
     * 
     * @param name the name of project.
     * @param open true to open the project if not already open, or false to do
     *            nothing.
     * @return the project which may or may not exist.
     * @throws Exception
     */
    public IProject getProject(String name, boolean open) throws Exception {
        IProject project = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(name);
        if (open && project.exists() && !project.isOpen()) {
            project.open(null);
        }
        return project;
    }

    /**
     * Gets the absolute file path.
     * 
     * @param project the file's project.
     * @param file the file.
     * @return the absolute file path.
     * @throws Exception
     */
    public String getFilePath(String project, String file) throws Exception {
        return getFilePath(getProject(project), file);
    }

    /**
     * Gets the absolute file path.
     * 
     * @param project the file's project.
     * @param file the file.
     * @return the absolute file path.
     * @throws Exception
     */
    public String getFilePath(IProject project, String file) throws Exception {
        file = file.replace('\\', '/');
        if (file.startsWith("/" + project.getName())) {
            if (file.startsWith("/" + project.getName() + "/")) {
                file = file.substring(2 + project.getName().length());
            } else if (file.endsWith("/" + project.getName())) {
                file = file.substring(1 + project.getName().length());
            }

            // path is the project root.
            if (file.length() == 0) {
                return getPath(project);
            }
        } else if (file.startsWith("/")
                || file.toLowerCase().startsWith("jar:")
                || file.toLowerCase().startsWith("zip:")) {
            return file;
        }

        String projectPath = getPath(project);
        if (file.toLowerCase().startsWith(projectPath.toLowerCase())) {
            return file;
        }

        IFile ifile = project.getFile(file);
        return ifile.getLocation().toOSString().replace('\\', '/');
    }

    /**
     * Gets the path on disk to the directory of the supplied project.
     * 
     * @param project the project name.
     * @return the path or null if not found.
     * @throws Exception
     */
    public String getPath(String project) throws Exception {
        return getPath(getProject(project));
    }

    /**
     * Gets the path on disk to the directory of the supplied project.
     * 
     * @param project the project.
     * @return the path or null if not found.
     * @throws Exception
     */
    public String getPath(IProject project) throws Exception {
        IPath path = getIPath(project);
        return null != path ? path.toOSString().replace('\\', '/') : null;
    }

    /**
     * Gets the path on disk to the directory of the supplied project.
     * 
     * @param projectName the project name.
     * @return the path or null if not found.
     * @throws Exception
     */
    public IPath getIPath(String projectName) throws Exception {
        return getIPath(getProject(projectName));
    }

    /**
     * Gets the path on disk to the directory of the supplied project.
     * 
     * @param project the project.
     * @return the path or null if not found.
     * @throws Exception
     */
    public IPath getIPath(IProject project) throws Exception {
        IPath path = project.getRawLocation();

        // eclipse returns null for raw location if project is under the
        // workspace.
        if (null == path) {
            String name = project.getName();
            path = ResourcesPlugin.getWorkspace().getRoot().getRawLocation();
            path = path.append(name);
        }
        return path;
    }

    /**
     * Gets the full path from the supplied path by removing any trailing path
     * segments that do not have a traling separator.
     * 
     * <pre>
     * service.getFullPath("/a/b/c/") : "/a/b/c/"
     * service.getFullPath("/a/b/c") : "/a/b/"
     * service.getFullPath("/a/b/c.txt") : "/a/b/"
     * </pre>
     * 
     * @param path the path.
     * @return the full path.
     */
    public String getFullPath(String path) {
        IPath p = Path.fromOSString(path);
        if (!p.hasTrailingSeparator()) {
            p = p.uptoSegment(p.segmentCount() - 1);
        }

        return p.addTrailingSeparator().toOSString();
    }

    /**
     * Gets the file extension from the supplied path.
     * 
     * <pre>
     * service.getExtension("/a/b/c/") : ""
     * service.getExtension("/a/b/c" : ""
     * service.getExtension("/a/b/c.txt") : "txt"
     * </pre>
     * 
     * @param path the path.
     * @return the file extension.
     */
    public String getExtension(String path) {
        String ext = Path.fromOSString(path).getFileExtension();
        if (null == ext) {
            return StringUtils.EMPTY;
        }
        return ext;
    }

    /**
     * Translates a file name that does not conform to the standard url file
     * format.
     * <p/>
     * Main purpose is to convert paths like:<br/>
     * <code>/opt/sun-jdk-1.5.0.05/src.zip/javax/swing/Spring.java</code><br/>
     * to<br/>
     * <code>zip:file:///opt/sun-jdk-1.5.0.05/src.zip!/javax/swing/Spring.java</code>
     * 
     * @param file the file to translate.
     * @return the translated file.
     */
    public String toUrl(String file) {
        file = file.replace('\\', '/');

        // if the path points to a real file, return it.
        if (new File(file).exists()) {
            return file;
        }

        // already an url.
        if (file.startsWith(JAR_PREFIX) || file.startsWith(ZIP_PREFIX)) {
            return file;
        }

        // otherwise do some conversion.
        StringBuilder buffer = new StringBuilder();
        try {
            CompositeName fileName = new CompositeName(file);
            Enumeration<String> names = fileName.getAll();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                if (name.indexOf("$") != -1) {
                    name = name.substring(0, name.indexOf("$")) + '.'
                            + getExtension(name);
                }

                if (name.length() != 0) {
                    buffer.append('/').append(name);

                    if (!new File(buffer.toString()).exists()) {
                        String path = getFullPath(buffer.toString());

                        if (path.endsWith("/") || path.endsWith("\\")) {
                            path = path.substring(0, path.length() - 1);
                        }

                        if (path.endsWith(JAR_EXT)) {
                            buffer = new StringBuilder(JAR_PREFIX).append(path)
                                    .append('!').append('/').append(name);
                        } else if (path.endsWith(ZIP_EXT)) {
                            buffer = new StringBuilder(ZIP_PREFIX).append(path)
                                    .append('!').append('/').append(name);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return buffer.toString();
    }

    /**
     * Determines if the supplied path is a jar compatible path that can be
     * converted to a jar: url.
     * 
     * @param path the path.
     * @return true if a jar or zip, false otherwise.
     */
    protected boolean isJarArchive(IPath path) {
        String ext = path.getFileExtension();
        return null != ext && ext.toLowerCase().matches("^(jar|zip)$");
    }
}
