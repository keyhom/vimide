/**
 * Copyright (c) 2012 keyhom.c@gmail.com.
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
package org.vimide.eclipse.core.natures;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

/**
 * A mapping with eclipse's natures to human aliases.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class NaturesMapping implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * BI mapping.
     */
    private static final BiMap<String, String> mapping = HashBiMap.create();

    static {
        mapping.put("java", "org.eclipse.jdt.core.javanature");
        mapping.put("maven", "org.eclipse.m2e.core.maven2Nature");

        mapping.put("pde.feature", "org.eclipse.pde.FeatureNature");
        mapping.put("pde.plugin", "org.eclipse.pde.PluginNature");
        mapping.put("pde.updateSite", "org.eclipse.pde.UpdateSiteNature");
    }

    public static String getNatureId(String alias) {
        if (null != alias)
            return mapping.get(alias.trim());
        return null;
    }

    public static String[] getNatureIds(String... aliases) {
        final List<String> result = Lists.newArrayList();
        if (null != aliases)
            for (String alias : aliases) {
                String nature = mapping.get(alias.trim());
                if (!Strings.isNullOrEmpty(nature))
                    result.add(nature);
            }

        return result.isEmpty() ? null : result.toArray(new String[result
                .size()]);
    }

    public static boolean containsNatureId(String natureId) {
        return !Strings.isNullOrEmpty(getNatureAlias(natureId));
    }

    public static boolean containsNatureAlias(String alias) {
        return !Strings.isNullOrEmpty(getNatureId(alias));
    }

    public static String getNatureAlias(String natureId) {
        if (null != natureId)
            return mapping.inverse().get(natureId.trim());
        return null;
    }

    public static String[] getNatureAliases(String... natureIds) {
        final List<String> result = Lists.newArrayList();
        if (null != natureIds) {
            final BiMap<String, String> inverse = mapping.inverse();
            for (String nature : natureIds) {
                String alias = inverse.get(nature.trim());
                if (!Strings.isNullOrEmpty(alias))
                    result.add(alias);
            }

        }
        return result.isEmpty() ? null : result.toArray(new String[result
                .size()]);
    }

    public static void registerNatureMapping(String alias, String natureId) {
        if (!Strings.isNullOrEmpty(alias) && !Strings.isNullOrEmpty(natureId)) {
            mapping.put(alias, natureId);
        }
    }

    public static void unregisterNatureMapping(String alias) {
        if (!Strings.isNullOrEmpty(alias)) {
            mapping.remove(alias);
        }
    }

    public static void unregisterNatureMapping(String alias, String natureId) {
        if (!Strings.isNullOrEmpty(alias) && !Strings.isNullOrEmpty(natureId)) {
            if (natureId.equals(mapping.get(alias))) {
                mapping.remove(alias);
            }
        }
    }
    
    public static void clearAll() {
        mapping.clear();
    }

    /**
     * Creates an new NaturesMapping instance.
     */
    private NaturesMapping() {
        super();
    }

}
