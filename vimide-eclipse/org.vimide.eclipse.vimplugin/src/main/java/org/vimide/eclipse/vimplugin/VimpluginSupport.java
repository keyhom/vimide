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
package org.vimide.eclipse.vimplugin;

import org.vimide.eclipse.vimplugin.preferences.VimpluginPreferenceConstants;
import org.vimide.vimplugin.VimSupport;

/**
 * Support for vimplugin.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimpluginSupport {

    private static VimSupport support;

    /**
     * Gets the vim support determiner.
     * 
     * @return vim support determiner.
     */
    private static VimSupport getSupport() {
        if (null == support) {
            String gvim = VimideVimpluginPlugin.getDefault()
                    .getPreferenceStore()
                    .getString(VimpluginPreferenceConstants.P_GVIM);
            support = new VimSupport(gvim);
        }
        return support;
    }

    /**
     * Creates an new VimpluginSupport instance.
     */
    private VimpluginSupport() {
        super();
    }

    /**
     * Determines if the configured gvim path available.
     * 
     * @return true if the configured gvim path available, false otherwise.
     */
    public static boolean isGvimAvailable() {
        return getSupport().isAvailableExecutable();
    }

    /**
     * Determines if the configured gvim instance supports embedding.
     * 
     * @return true if embedding is supported, false otherwise.
     */
    public static boolean isEmbedSupported() {
        return getSupport().isEmbedEnabled();
    }

    /**
     * Determines if the configured gvim instance supports the required netbeans
     * interface.
     * 
     * @return true if netbeans supported, false otherwise.
     */
    public static boolean isNbSupported() {
        return getSupport().isNbEnabled();
    }

    /**
     * Determines if the configured gvim can <a href="">reliably</a> support the
     * netbeans document listening events.
     * 
     * @return true if document listening is reliably supported, false
     *         otherwise.
     */
    public static boolean isNbDocumentListenSupported() {
        return getSupport().isNbDocumentListenEnabled();
    }

    /**
     * Resets the features cache.
     */
    public static void reset() {
        support = null;
    }

}
