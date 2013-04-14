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
package org.vimide.eclipse.flashbuilder.search;

import java.util.Set;

import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.dialogs.SearchPattern;

import com.adobe.flexbuilder.codemodel.common.CMFactory;
import com.adobe.flexbuilder.codemodel.definitions.IDefinition;
import com.adobe.flexbuilder.codemodel.project.IProject;
import com.adobe.flexbuilder.codemodel.tree.IASNode;
import com.adobe.flexbuilder.codemodel.tree.IProjectRootNode;
import com.adobe.flexide.as.core.ui.dialogs.ASTypesLabelProvider;
import com.adobe.flexide.as.core.ui.dialogs.CamelCaseFilterMatcher;
import com.adobe.flexide.as.core.ui.dialogs.ClassAndInterfaceProvider;
import com.adobe.flexide.as.core.ui.dialogs.IOpenTypeLabelProvider;
import com.adobe.flexide.editorcore.document.IFlexDocument;
import com.adobe.flexide.editorcore.loadservice.CMLoadServiceProvider;
import com.google.common.collect.Sets;

/**
 * Represents a manager for searching type, interface, method, etc.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class SearchManager {

    /**
     * Singleton holder.
     */
    public static class SingletonHolder {
        static final SearchManager instance = new SearchManager();
    }

    /**
     * Gets the singleton manager.
     * 
     * @return singleton instance of SearchManager.
     */
    public static SearchManager getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Private Constructor.
     */
    private SearchManager() {
        super();
    }

    public Set<IDefinition> getDefinitions(IFlexDocument document, int offset, String pattern) {
        final Set<IDefinition> results = Sets.newHashSet();
        if (null != document) {
            ClassAndInterfaceProvider fDataProvider = new ClassAndInterfaceProvider(true, true);
            CMLoadServiceProvider.getLoadService().loadProject(document.getIFile().getProject());
            IProject project = null;

            synchronized (CMFactory.getLockObject()) {
                project = CMFactory.getManager().getProjectFor(document.getIFile().getProject());
            }

            if (null != project) {
                fDataProvider.setShowFinalClasses(true);
                fDataProvider.setIncludeExcludedTypes(true);
                IDefinition[] definitions = fDataProvider.getDefinitions(project.getSpecification());
                ItemFilter itemFilter = this.new ItemFilter(pattern);
                for (IDefinition def : definitions) {
                    if (itemFilter.matchItem(def)) {
                        results.add(def);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Filter for AS matching.
     */
    public class ItemFilter {

        protected SearchPattern patternMatcher;
        protected IOpenTypeLabelProvider fLabelProvider = new ASTypesLabelProvider();
        protected FilteredList.FilterMatcher fMatcher = new CamelCaseFilterMatcher();

        /**
         * Creates an ItemFilter by the specific search pattern.
         * 
         * @param searchPattern the search pattern.
         */
        public ItemFilter(SearchPattern searchPattern) {
            if (null == searchPattern)
                throw new IllegalArgumentException("searchPattern");
            this.patternMatcher = searchPattern;
        }

        /**
         * Creates an ItemFilter by the specific string pattern
         * 
         * @param stringPattern
         */
        public ItemFilter(String stringPattern) {
            this.patternMatcher = new SearchPattern();
            this.patternMatcher.setPattern(stringPattern);
        }

        public boolean isSubFilter(ItemFilter filter) {
            if (null != filter) {
                return this.patternMatcher.isSubPattern(filter.patternMatcher);
            }
            return false;
        }

        public boolean equalsFilter(ItemFilter filter) {
            return ((null != filter) && (filter.patternMatcher
                    .equalsPattern(this.patternMatcher)));
        }

        public boolean isCamelCasePattern() {
            return this.patternMatcher.getMatchRule() == 128;
        }

        public String getPattern() {
            return this.patternMatcher.getPattern();
        }

        public int getMatchRule() {
            return this.patternMatcher.getMatchRule();
        }

        protected boolean matches(String text) {
            return this.patternMatcher.matches(text);
        }

        public boolean matchesRawNamePattern(Object item) {
            String prefix = this.patternMatcher.getPattern();
            String text = getElementName(item);

            if (null == text) {
                return false;
            }

            int textLength = text.length();
            int prefixLength = prefix.length();

            if (textLength < prefixLength) {
                return false;
            }

            for (int i = prefixLength - 1; i >= 0; --i) {
                if (Character.toLowerCase(prefix.charAt(i)) != Character
                        .toLowerCase(text.charAt(i)))
                    return false;
            }

            return true;
        }

        public String getElementName(Object paramObject) {
            synchronized (CMFactory.getLockObject()) {
                return fLabelProvider.getText(paramObject);
            }
        }

        public boolean matchItem(Object paramObject) {
            synchronized (CMFactory.getLockObject()) {
                fMatcher.setFilter(getPattern() + "$", false, true);
                return fMatcher.match(paramObject);
            }
        }

        public boolean isConsistentItem(Object paramObject) {
            if (paramObject instanceof IASNode) {
                IASNode node = ((IASNode) paramObject)
                        .getAncestorOfType(IProjectRootNode.class);
                if (null != node) {
                    return (((IProjectRootNode) node).getProject()
                            .getSpecification() != null);
                }
            }
            return false;
        }
    }
}

// vim:ft=java
