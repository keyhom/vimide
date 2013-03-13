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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.vimide.eclipse.jdt.util.TypeUtil;

/**
 * Represents the informations object for the bean properties operation.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class BeanProperties {

    public static final String INT_SIGNATURE = Signature.createTypeSignature(
            "int", true);
    public static final String[] INT_ARGUMENT = new String[] { INT_SIGNATURE };
    public static final IField[] NO_FIELDS = new IField[0];

    public static final int TYPE_GET = 0;
    public static final int TYPE_GET_INDEX = 1;
    public static final int TYPE_SET = 2;
    public static final int TYPE_SET_INDEX = 3;

    private ICompilationUnit src;
    private int offset = 0;;
    private int methods;
    private boolean indexed = false;
    private String[] fields;

    /**
     * Creates a BeanProperties object.
     * 
     * @param src the source file.
     * @param offset the offset the element located at.
     * @param methods the methods which make to generate.
     * @param indexed the sign for indexing.
     * @param fields the array of fields.
     */
    public BeanProperties(ICompilationUnit src, int offset, int methods,
            boolean indexed, String[] fields) {
        this.src = src;
        this.offset = offset;
        this.methods = methods;
        this.indexed = indexed;
        this.fields = fields;
    }

    /**
     * Gets the compilation unit.
     * 
     * @return the compilation unit.
     */
    public ICompilationUnit getCompilationUnit() {
        return src;
    }

    /**
     * Determines if contains the getter generation.
     * 
     * @return true if contains the getter generation, false otherwise.
     */
    public boolean containsGetter() {
        return methods == 1 || methods == 0;
    }

    /**
     * Determines if contains the setter generation.
     * 
     * @return true if contains the setter generation, false otherwise.
     */
    public boolean containsSetter() {
        return methods == 2 || methods == 0;
    }

    /**
     * Gets the value of the offset property.
     * 
     * @return the offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Determines if indexed was.
     * 
     * @return true if indexed was, false otherwise.
     */
    public boolean isIndexed() {
        return indexed;
    }

    /**
     * Gets the array of fields.
     * 
     * @return fields.
     */
    public String[] getFields() {
        if (null == fields)
            return new String[] {};
        return fields;
    }

    /**
     * Gets the type of enclosing element.
     * 
     * @return the enclosing type.
     */
    public IType getEnclosingType() {
        try {
            return TypeUtil.getType(src, offset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

// vim:ft=java
