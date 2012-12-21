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

import org.eclipse.osgi.util.NLS;
import org.vimide.eclipse.core.CoreMessages;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimpluginMessage extends CoreMessages {

    static final String BUNDLE_NAME = "org.vimide.eclipse.vimplugin.messages";

    /**
     * Gvim messages.
     */
    public static String gvim_not_found;
    public static String gvim_not_supported;
    public static String gvim_embed_not_supported;
    public static String gvim_nb_not_enabled;
    public static String gvim_startup_failed;
    public static String gvim_startup_done_event;
    public static String gvim_external_success;
    public static String gvim_embed_unsupported;
    public static String gvim_embed_fallback;

    /**
     * Preferences messages.
     */
    public static String preferences_description;
    public static String preferences_embed;
    public static String preferences_tabbed;
    public static String preferences_focus_click;
    public static String preferences_port;
    public static String preferences_gvim;
    public static String preferences_gvim_args;

    static {
        NLS.initializeMessages(BUNDLE_NAME, VimpluginMessage.class);
    }

}
