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
package org.vimide.eclipse.jdt.complete;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.vimide.eclipse.core.complete.CodeCompletionResult;

/**
 * Comparator for sorting completion results.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class CodeCompletionComparator implements
        Comparator<CodeCompletionResult> {

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(CodeCompletionResult o1, CodeCompletionResult o2) {
        if (null == o1 && null == o2)
            return 0;
        else if (null == o2)
            return -1;
        else if (null == o1)
            return 1;

        // push keyword to the end.
        if (CodeCompletionResult.KEYWORD == o1.getType()
                && CodeCompletionResult.KEYWORD != o2.getType()) {
            return 1;
        } else if (CodeCompletionResult.KEYWORD == o2.getType()
                && CodeCompletionResult.KEYWORD != o1.getType()) {
            return -1;
        }

        return Collator.getInstance(Locale.US).compare(
                new String(o1.getCompletion()), new String(o2.getCompletion()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof CodeCompletionComparator)
            return true;

        return false;
    }
}
