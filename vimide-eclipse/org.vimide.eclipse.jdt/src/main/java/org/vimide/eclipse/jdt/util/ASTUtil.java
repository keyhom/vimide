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
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

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
        return null;
    }
}
