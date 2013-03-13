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

import java.util.List;

/**
 * Represents the container for the result of implementation manipulation.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class ImplementationResult {

    private String type;
    private List<ImplementationType> superTypes;

    /**
     * Default constructor.
     * 
     * @param type the name of type.
     * @param superTypes the list of supertype.
     */
    public ImplementationResult(String type, List<ImplementationType> superTypes) {
        this.type = type;
        this.superTypes = superTypes;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the superTypes
     */
    public List<ImplementationType> getSuperTypes() {
        return superTypes;
    }

}

// vim:ft=java
