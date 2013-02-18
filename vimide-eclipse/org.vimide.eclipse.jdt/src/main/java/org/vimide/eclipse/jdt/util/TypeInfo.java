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

import java.util.Arrays;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jdt.core.IType;

/**
 * Class which encapsulates a type and its generic info.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class TypeInfo {

    private IType type;
    private String[] typeParameters;
    private String[] typeArguments;

    /**
     * Creates an new TypeInfo instance.
     * 
     * @param type
     * @param typeParameters
     * @param typeArguments
     */
    public TypeInfo(IType type, String[] typeParameters, String[] typeArguments) {
        super();
        this.type = type;
        this.typeParameters = typeParameters;
        this.typeArguments = typeArguments;
    }

    /**
     * Gets the value of type property.
     * 
     * @return the type
     */
    public IType getType() {
        return type;
    }

    /**
     * Gets the value of typeParameters property.
     * 
     * @return the typeParameters
     */
    public String[] getTypeParameters() {
        return typeParameters;
    }

    /**
     * Gets the value of typeArguments property.
     * 
     * @return the typeArguments
     */
    public String[] getTypeArguments() {
        return typeArguments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(24, 56).append(type).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TypeInfo))
            return false;
        if (this == other)
            return true;

        TypeInfo result = (TypeInfo) other;
        return new EqualsBuilder().append(getType(), result.getType())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder()
                .append(null != type ? type.getElementName() : "null")
                .append(":").append(" params: ")
                .append(Arrays.toString(typeParameters)).append(" args: ")
                .append(Arrays.toString(typeArguments)).toString();
    }
}
