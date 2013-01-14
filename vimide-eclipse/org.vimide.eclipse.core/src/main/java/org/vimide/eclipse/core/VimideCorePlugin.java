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

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.server.VimideHttpServer;
import org.vimide.eclipse.core.servlet.JobsServlet;
import org.vimide.eclipse.core.servlet.PingServlet;
import org.vimide.eclipse.core.servlet.ShellServlet;
import org.vimide.eclipse.core.servlet.TestServlet;
import org.vimide.eclipse.core.servlet.WorkspaceDirServlet;
import org.vimide.eclipse.core.servlet.project.ProjectByResourceServlet;
import org.vimide.eclipse.core.servlet.project.ProjectCloseServlet;
import org.vimide.eclipse.core.servlet.project.ProjectImportServlet;
import org.vimide.eclipse.core.servlet.project.ProjectInfoServlet;
import org.vimide.eclipse.core.servlet.project.ProjectListServlet;
import org.vimide.eclipse.core.servlet.project.ProjectNamesServlet;
import org.vimide.eclipse.core.servlet.project.ProjectOpenServlet;
import org.vimide.eclipse.core.servlet.project.ProjectRefreshServlet;
import org.vimide.eclipse.core.servlet.project.ProjectsServlet;

/**
 * Core Plugin.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimideCorePlugin extends VimidePlugin {

    /**
     * Logger
     */
    static final Logger LOGGER = LoggerFactory
            .getLogger(VimideCorePlugin.class);

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        VimideHttpServer.getInstance().stop();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.vimide.eclipse.core.VimidePlugin#activate(org.osgi.framework.BundleContext)
     */
    @Override
    protected void activate(BundleContext bundleContext) {
        try {
            // startup the httpd first.
            VimideHttpServer.getInstance().start(3333);

            super.activate(bundleContext);
        } catch (Exception e) {
            LOGGER.error("Exception caught: {}", e.getMessage(), e);
        }

        VimideHttpServer.getInstance().registerServlet(PingServlet.class);
        VimideHttpServer.getInstance().registerServlet(ShellServlet.class);
        VimideHttpServer.getInstance().registerServlet(JobsServlet.class);
        VimideHttpServer.getInstance().registerServlet(
                WorkspaceDirServlet.class);
        VimideHttpServer.getInstance()
                .registerServlet(ProjectListServlet.class);
        VimideHttpServer.getInstance()
                .registerServlet(ProjectInfoServlet.class);
        VimideHttpServer.getInstance().registerServlet(
                ProjectImportServlet.class);
        VimideHttpServer.getInstance().registerServlet(ProjectsServlet.class);
        VimideHttpServer.getInstance().registerServlet(
                ProjectByResourceServlet.class);
        VimideHttpServer.getInstance().registerServlet(
                ProjectCloseServlet.class);
        VimideHttpServer.getInstance()
                .registerServlet(ProjectOpenServlet.class);
        VimideHttpServer.getInstance().registerServlet(
                ProjectRefreshServlet.class);
        VimideHttpServer.getInstance().registerServlet(TestServlet.class);
        VimideHttpServer.getInstance().registerServlet(ProjectNamesServlet.class);
    }

}
