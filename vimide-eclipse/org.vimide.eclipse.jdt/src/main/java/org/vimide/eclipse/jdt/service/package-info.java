package org.vimide.eclipse.jdt.service;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import com.google.common.collect.Lists;

/**
 * Utilities for working with java element.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
class ImportUtil {

    public static boolean importsInSameGroup(int separationLevel,
            ImportDeclaration i1, ImportDeclaration i2) {
        // -1 = separate based on full package.
        // 0 = never separate
        // n = separate on comparing of n segments of the package.

        if (0 == separationLevel) {
            return true;
        }

        List<String> pn1 = packageName(i1);
        List<String> pn2 = packageName(i2);

        for (int i = 0; i < separationLevel || separationLevel == -1; i++) {
            int level = i + 1;
            if (pn1.size() < level) {
                return pn2.size() < level;
            }

            if (pn2.size() < level) {
                return pn1.size() < level;
            }

            if (!pn1.get(i).equals(pn2.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static List<String> packageName(ImportDeclaration imprt) {
        String name = imprt.getName().getFullyQualifiedName();

        List<String> pack = Lists.newArrayList();
        for (String part : org.apache.commons.lang.StringUtils.split(name, '.')) {
            if (java.lang.Character.isUpperCase(part.charAt(0))) {
                break;
            }

            pack.add(part);
        }
        return pack;
    }

    public static boolean isImportExcluded(IProject project, String name)
            throws Exception {
        // TODO: required the settings to determines whether excluded the
        // importing by the type.
        return false;
    }
}
