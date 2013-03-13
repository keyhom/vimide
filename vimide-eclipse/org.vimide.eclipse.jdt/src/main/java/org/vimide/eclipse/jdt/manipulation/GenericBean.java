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
package org.vimide.eclipse.jdt.manipulation;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Represents a abstract javabean for generic code manipulation.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class GenericBean {

    private ICompilationUnit src;

    /**
     * Creates a javabean by the supplied compilation unit.
     * 
     * @param src the compilation unit.
     */
    public GenericBean(ICompilationUnit src) {
        this.src = src;
    }

    /**
     * @return the compilation unit
     */
    public ICompilationUnit getCompilationUnit() {
        return src;
    }

    /**
     * @param src the compilation unit to set
     */
    public void setCompilationUnit(ICompilationUnit src) {
        this.src = src;
    }

    /**
     * @return the javaProject
     */
    public IJavaProject getJavaProject() {
        return getCompilationUnit().getJavaProject();
    }

    /**
     * @return the project
     */
    public IProject getProject() {
        return getJavaProject().getProject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }

}

// vim:ft=java
