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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

/**
 * Utility class for working with the eclipse java dom model.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class ASTUtil {

    /**
     * Creates an new ASTUtil instance in private mode.
     */
    private ASTUtil() {
    }

    /**
     * Gets the compilation unit for the supplied ICompilationUnit source.
     * <p/>
     * Equivalent of getCompilationUnit(src, false)
     * 
     * @param src the ICompilationUnit source.
     * @return a AST compilation unit.
     */
    public static CompilationUnit getCompilationUnit(ICompilationUnit src) {
        return getCompilationUnit(src, false);
    }

    /**
     * Gets the compilation unit for the supplied ICompilationUnit source.
     * 
     * @param src the ICompilationUnit source.
     * @param recordModifications true to record any modifications, false
     *            otherwise.
     * @return a AST compilation unit.
     */
    public static CompilationUnit getCompilationUnit(ICompilationUnit src,
            boolean recordModifications) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(src);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        if (recordModifications)
            cu.recordModifications();
        return cu;
    }

    /**
     * Commits any changes made to the supplied CompilationUnit.
     * 
     * @param src the original source.
     * @param node the compilation unit AST node.
     * @throws Exception
     */
    public static void commitCompilationUnit(ICompilationUnit src,
            CompilationUnit node) throws Exception {
        Document document = new Document(src.getBuffer().getContents());
        TextEdit edits = node.rewrite(document, src.getJavaProject()
                .getOptions(true));
        edits.apply(document);
        src.getBuffer().setContents(document.get());
        if (src.isWorkingCopy()) {
            src.commitWorkingCopy(false, null);
        }
        src.save(null, false);
    }

    /**
     * Finds the node at the specified offset.
     * 
     * @param root the root node to find.
     * @param offset the node offset in the compilation unit.
     * @return the node at the specified offset.
     * @throws Exception
     */
    public static ASTNode findNode(CompilationUnit root, int offset)
            throws Exception {
        NodeFinder finder = new NodeFinder(root, offset, 1);
        return finder.getCoveredNode();
    }

    /**
     * Finds the node at the specified offset that matches up with the specified
     * IJavaElement.
     * 
     * @param root the root node to find.
     * @param offset the node offset in the root.
     * @param element the IJavaElement to match.
     * @return the node at the specified offset.
     * @throws Exception
     */
    public static ASTNode findNode(CompilationUnit root, int offset,
            IJavaElement element) throws Exception {
        ASTNode node = findNode(root, offset);
        if (null == node)
            return null;
        if (element.getElementType() == IJavaElement.TYPE_PARAMETER) {
            element = element.getParent();
        }

        switch (element.getElementType()) {
            case IJavaElement.PACKAGE_DECLARATION:
                node = resolveNode(node, PackageDeclaration.class);
                break;
            case IJavaElement.IMPORT_DECLARATION:
                node = resolveNode(node, ImportDeclaration.class);
                break;
            case IJavaElement.TYPE:
                node = resolveNode(node, AbstractTypeDeclaration.class);
                break;
            case IJavaElement.INITIALIZER:
                node = resolveNode(node, Initializer.class);
                break;
            case IJavaElement.FIELD:
                node = resolveNode(node, FieldDeclaration.class);
                break;
            case IJavaElement.METHOD:
                node = resolveNode(node, MethodDeclaration.class);
                break;
            default:
                System.out
                        .println("findNode(CompilationUnit, int, IJavaElement) - unrecognized element type "
                                + element.getElementType());
                break;
        }
        return node;
    }

    /**
     * Walk up the node tree until a node of the specified type is reached.
     * 
     * @param node the starting node.
     * @param type the type to resolve.
     * @return the resulting node.
     */
    private static ASTNode resolveNode(ASTNode node, Class<?> type)
            throws Exception {
        if (null == node)
            return null;
        if (type.isAssignableFrom(node.getClass())) {
            return node;
        }

        return resolveNode(node.getParent(), type);
    }
}
