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

/**
 * Represents a super class/interface containing methods that can be
 * overridden/implemented.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class ImplementationType {

    private String packageName;
    private String signature;
    private String[] methods;

    /**
     * Constructs a new {@link ImplementationType} instance.
     * 
     * @param packageName the name of package for this instance.
     * @param signature the signature for this instance.
     * @param methods the methods for this instance.
     */
    public ImplementationType(String packageName, String signature,
            String[] methods) {
        this.packageName = packageName;
        this.signature = signature;
        this.methods = methods;
    }

    /**
     * @return the packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @return the methods
     */
    public String[] getMethods() {
        return methods;
    }

}
