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
package org.vimide.eclipse.core;

import java.net.URL;
import java.util.Set;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.server.VimideHttpServer;

/**
 * Activator for Vimide plugins.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimidePlugin extends Plugin implements BundleListener {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(VimidePlugin.class);

    private static VimidePlugin plugin;

    /**
     * Retrieves the shared instance.
     * 
     * @return the shared instance.
     */
    public static VimidePlugin getDefault() {
        return plugin;
    }

    /**
     * Creates an new VimidePlugin instance.
     */
    public VimidePlugin() {
        super();

        plugin = this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        LOGGER.debug("{}: start", context.getBundle().getSymbolicName());
        super.start(context);

        context.addBundleListener(this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {

        LOGGER.debug("{}: stop", context.getBundle().getSymbolicName());
        super.stop(context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
     */
    @Override
    public void bundleChanged(BundleEvent event) {
        final Bundle bundle = event.getBundle();
        if (this.getBundle().equals(bundle)) {
            String state = "unknown";
            switch (bundle.getState()) {
                case Bundle.ACTIVE:
                    state = "active";
                    break;
                case Bundle.INSTALLED:
                    state = "installed";
                    break;
                case Bundle.RESOLVED:
                    state = "resolved";
                    break;
                case Bundle.STARTING:
                    state = "starting";
                    break;
                case Bundle.STOPPING:
                    state = "stopping";
                    break;
                case Bundle.UNINSTALLED:
                    state = "uninstalled";
                    break;
            }

            LOGGER.debug("{}: bundleChanged: {}", bundle.getSymbolicName(),
                    state);

            if (bundle.getState() == Bundle.ACTIVE) {
                this.activate(bundle.getBundleContext());
            }
        }
    }

    /**
     * Invoked when the bundle is activated by the bundle listener registered
     * during start(BundleContext).
     * 
     * @param context the bundle context.
     */
    @SuppressWarnings("unchecked")
    protected void activate(BundleContext context) {
        // resolves the bundle's classes and find the servlets.
        final String name = context.getBundle().getSymbolicName();

        try {
            final Class<? extends VimidePlugin> pluginClass = this.getClass();
            final ClassLoader classLoader = pluginClass.getClassLoader();

            String resourceName = pluginClass.getName().replace('.', '/');
            resourceName = resourceName.substring(0,
                    resourceName.lastIndexOf('/'));
            final URL resource = classLoader.getResource(resourceName);

            AnnotationDB db = new AnnotationDB();
            db.setScanClassAnnotations(true);
            db.setScanFieldAnnotations(false);
            db.setScanMethodAnnotations(false);
            db.setScanParameterAnnotations(false);
            db.scanArchives(resource);

            final Set<String> servletClasses = db.getAnnotationIndex().get(
                    WebServlet.class.getName());

            if (null != servletClasses) {
                for (String servletClass : servletClasses) {
                    LOGGER.debug("{}: resolving servlet: {}", name,
                            servletClass);
                    Class<? extends HttpServlet> loadClass = (Class<? extends HttpServlet>) classLoader
                            .loadClass(servletClass);
                    VimideHttpServer.getInstance().registerServlet(loadClass);
                }
            } else {
                LOGGER.debug("{}: no servlet found.", name);
            }
        } catch (final Exception e) {
            LOGGER.error("Unable to resolve the classes for auto-parse: {}",
                    e.getMessage());
        }
    }
}
