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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.eclipse.vimplugin.editors.VimEditorPartListener;
import org.vimide.eclipse.vimplugin.preferences.VimpluginPreferenceConstants;
import org.vimide.vimplugin.server.VimServer;

/**
 * Vim plugin to embed the vim/gvim into the eclipse by vimide.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimideVimpluginPlugin extends AbstractUIPlugin {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(VimideVimpluginPlugin.class);

    /**
     * Storage the plugin instance.
     */
    static VimideVimpluginPlugin plugin;

    /**
     * Gets the plugin instance.
     * 
     * @return the plugin instance.
     */
    public static VimideVimpluginPlugin getDefault() {
        return plugin;
    }

    private Properties properties;
    private VimServer vimServer;
    private AtomicInteger numberOfBufers = new AtomicInteger(0);

    private VimEditorPartListener partListener;

    /**
     * Creates an new VimideVimpluginPlugin instance.
     */
    public VimideVimpluginPlugin() {
        super();

        plugin = this;

        properties = new Properties();
        try {
            properties.load(getClass()
                    .getResourceAsStream("/plugin.properties"));
        } catch (final IOException e) {
            MessageDialog.openError(getWorkbench().getActiveWorkbenchWindow()
                    .getShell(), "Vimplugin",
                    "Unable to load plugin.properties");
            LOGGER.error(
                    "Error caught at unable to load plugin.properties: {}",
                    e.getMessage(), e);
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        // vim start at buffer 1.
        getNumberOfBuffers().set(1);

        vimServer = new VimServer();
        startVimServer();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);

        plugin = null;
        vimServer.stop();
    }

    public AtomicInteger getNumberOfBuffers() {
        return numberOfBufers;
    }

    /**
     * Starts the vim server.
     */
    protected void startVimServer() {
        if (null != vimServer) {
            stopVimServer(); // stop the vimServer first.

            int port = getPreferenceStore().getInt(
                    VimpluginPreferenceConstants.P_PORT);

            vimServer.start(new InetSocketAddress(port));
        }
    }

    /**
     * Stops the vim server.
     */
    protected void stopVimServer() {
        if (null != vimServer) {
            vimServer.stop();
        }
    }

    public IPartListener getPartListener() {
        return partListener;
    }

    /**
     * Gets the specified property from plugin.properties.
     * 
     * @param name the property name.
     * @return the property value or null if not def.
     */
    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    /**
     * Gets the specified property from the plugin.properties.
     * 
     * @param name the property name.
     * @param defaultValue the default value if property is not found.
     * @return the property value of default value if not found.
     */
    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public VimServer getVimServer() {
        return vimServer;
    }

}
