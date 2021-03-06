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
package org.vimide.eclipse.jdt;

import org.eclipse.osgi.util.NLS;
import org.vimide.eclipse.core.CoreMessages;

/**
 * JDT plugin message bindings.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class JdtMessages extends CoreMessages {

    public static final String BUNDLE_NAME = "org.vimide.eclipse.jdt.messages";

    public static String class_not_found;
    public static String type_not_found;
    public static String type_not_a_class;
    public static String src_contains_errors;
    public static String constructor_already_exists;
    public static String field_not_found;
    public static String not_a_field;
    public static String check_import;

    static {
        NLS.initializeMessages(BUNDLE_NAME, JdtMessages.class);
    }

    /**
     * Creates a JdtMessages object by private.
     */
    private JdtMessages() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(JdtMessages.class.getName()).append(": [")
                .append(BUNDLE_NAME).append("]");
        return sb.toString();
    }

}
