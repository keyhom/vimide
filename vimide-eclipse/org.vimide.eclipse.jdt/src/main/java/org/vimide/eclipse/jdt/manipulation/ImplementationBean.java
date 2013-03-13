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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.vimide.eclipse.jdt.util.TypeUtil;

import com.google.common.base.Strings;

/**
 * Represents a java bean for implmenetaion / overriden manipulation.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class ImplementationBean extends GenericBean {

    private int offset;
    private String typeName;
    private String superTypeName;
    private String[] methodNames;

    /**
     * Creates a ImplementationBean object.
     * 
     * @param src the source file.
     * @param offset the offset the type element located.
     * @param typeName the name of the type.
     * @param superTypeName the name of the supertype.
     * @param methodNames the array of methods' name.
     */
    public ImplementationBean(ICompilationUnit src, int offset,
            String typeName, String superTypeName, String... methodNames) {
        super(src);
        this.offset = offset;
        this.typeName = typeName;
        this.superTypeName = superTypeName;
        this.methodNames = methodNames;
    }

    /**
     * Gets the type.
     * 
     * @return the type.
     * @throws Exception
     */
    public IType getType() throws Exception {
        if (!Strings.isNullOrEmpty(typeName)) {
            return getJavaProject().findType(getTypeName().replace('$', '.'));
        }
        return TypeUtil.getType(getCompilationUnit(), offset);
    }

    /**
     * Gets the supertype.
     * 
     * @return the supertype.
     */
    public IType getSuperType() {
        return null;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @param offset the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @return the typeName
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * @param typeName the typeName to set
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * @return the superTypeName
     */
    public String getSuperTypeName() {
        return superTypeName;
    }

    /**
     * @param superTypeName the superTypeName to set
     */
    public void setSuperTypeName(String superTypeName) {
        this.superTypeName = superTypeName;
    }

    /**
     * @return the methodNames
     */
    public String[] getMethodNames() {
        return methodNames;
    }

    /**
     * @param methodNames the methodNames to set
     */
    public void setMethodNames(String[] methodNames) {
        this.methodNames = methodNames;
    }
}
// vim:ft=java
