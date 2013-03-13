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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a static facade for manipulating the source generating,
 * formatting, sorting etc.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class Manipulator {

    /**
     * Logger.
     */
    static final Logger log = LoggerFactory.getLogger(Manipulator.class
            .getName());

    /**
     * Private constructor.
     */
    private Manipulator() {
        super();
    }

    /**
     * Generates the properties by the supplied information object.
     * <p/>
     * This method represent visiting the operation.
     * 
     * @param beanProperties the information object.
     * @return the result of generation.
     */
    public static Object generateProperties(BeanProperties beanProperties) {
        BeanPropertiesOperation op = new BeanPropertiesOperation();
        try {
            // something first.
            op.accept(beanProperties);
            // anything after operation.
            // resulting.
            return 1;
        } catch (final Exception e) {
            log.error("Generates the properties failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Makes a implementation/overriden manipulation.
     * 
     * @param bean the bean of implementation / overriden.
     * @return the result of manipulation.
     */
    public static Object makeImpl(ImplementationBean bean) {
        ImplementationManipulator manipulator = new ImplementationManipulator();
        if (null != bean)
            try {
                manipulator.accept(bean);
                return manipulator.getResult(bean.getCompilationUnit(),
                        bean.getType());
            } catch (Exception e) {
                log.error("Makking implementation / overriden failed: {}",
                        e.getMessage(), e);
            }
        return null;
    }
}
