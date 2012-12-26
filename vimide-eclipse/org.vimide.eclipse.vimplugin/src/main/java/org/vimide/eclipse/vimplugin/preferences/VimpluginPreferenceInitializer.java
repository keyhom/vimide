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
package org.vimide.eclipse.vimplugin.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.vimide.eclipse.vimplugin.VimideVimpluginPlugin;

/**
 * Initializes default preference values.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimpluginPreferenceInitializer extends
        AbstractPreferenceInitializer {

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        final VimideVimpluginPlugin plugin = VimideVimpluginPlugin.getDefault();
        IPreferenceStore preferenceStore = plugin.getPreferenceStore();
        preferenceStore.setDefault(VimpluginPreferenceConstants.P_PORT, 3219);
//        preferenceStore.setDefault(VimpluginPreferenceConstants.P_EMBED,
//                "true".equals(plugin.getProperty("gvim.embed.default")));
//        preferenceStore.setDefault(VimpluginPreferenceConstants.P_TABBED, true);
        preferenceStore.setDefault(
                VimpluginPreferenceConstants.P_FOCUS_AUTO_CLICK, true);
        preferenceStore.setDefault(VimpluginPreferenceConstants.P_GVIM,
                plugin.getProperty("gvim.default"));
    }

}
