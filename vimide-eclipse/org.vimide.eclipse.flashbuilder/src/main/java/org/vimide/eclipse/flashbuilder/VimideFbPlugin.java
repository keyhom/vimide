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
package org.vimide.eclipse.flashbuilder;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.eclipse.core.VimidePlugin;

/**
 * Represents a eclipse plugin for flashbuilder extension of Vimide.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimideFbPlugin extends VimidePlugin {

    /**
     * Logger
     */
    static final Logger log = LoggerFactory.getLogger(VimideFbPlugin.class
            .getName());

    /**
     * Storages for plugin instance.
     */
    private static VimideFbPlugin plugin;

    /**
     * Gets the plugin instance of {@link VimideFbPlugin}.
     * 
     * @return the plugin instance.
     */
    public static VimideFbPlugin getPlugin() {
        return plugin;
    }

    /**
     * Default constructor.
     */
    public VimideFbPlugin() {
        super();
        plugin = this;
    }

    @Override
    protected void activate(BundleContext context) {
        super.activate(context);

        // actived here.
    }

}
// vim:ft=java
