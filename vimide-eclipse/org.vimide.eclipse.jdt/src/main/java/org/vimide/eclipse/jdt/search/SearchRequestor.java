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
package org.vimide.eclipse.jdt.search;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.vimide.eclipse.jdt.util.IJavaElementComparator;

import com.google.common.collect.Lists;

/**
 * Extension to SearchRequestor that adds getMatches().
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class SearchRequestor extends
        org.eclipse.jdt.core.search.SearchRequestor {

    private static final SearchMatchComparator MATCH_COMPARATOR = new SearchMatchComparator();
    private List<SearchMatch> matches = Lists.newArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptSearchMatch(SearchMatch match) throws CoreException {
        if (SearchMatch.A_ACCURATE == match.getAccuracy())
            matches.add(match);
    }

    /**
     * Gets a list of all the matches found.
     * 
     * @return list of search matches.
     */
    public List<SearchMatch> getMatches() {
        Collections.sort(matches, MATCH_COMPARATOR);
        return matches;
    }

    /**
     * Comparator for search matches.
     * 
     * @author keyhom (keyhom.c@gmail.com)
     */
    public static class SearchMatchComparator implements
            Comparator<SearchMatch> {

        private static final IJavaElementComparator ELEMENT_COMPARATOR = new IJavaElementComparator();

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(SearchMatch o1, SearchMatch o2) {
            return ELEMENT_COMPARATOR.compare((IJavaElement) o1.getElement(),
                    (IJavaElement) o2.getElement());
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object obj) {
            if (obj instanceof SearchMatchComparator) {
                return true;
            }
            return false;
        }

    }
}
