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

import java.util.Comparator;

import org.eclipse.jdt.core.IJavaElement;

/**
 * Comparator for sorting IJavaElement(s).
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class IJavaElementComparator implements Comparator<IJavaElement> {

    /**
     * Creates a IJavaElementComparator object.
     */
    public IJavaElementComparator() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(IJavaElement o1, IJavaElement o2) {
        if (null == o1 && null == o2)
            return 0;
        else if (null == o2)
            return -1;
        else if (null == o1)
            return 1;

        IJavaElement p1 = EclipseJdtUtil.getPrimaryElement(o1);
        IJavaElement p2 = EclipseJdtUtil.getPrimaryElement(o2);
        return p1.getElementType() - p2.getElementType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IJavaElementComparator)
            return true;
        return false;
    }

}
