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
package org.vimide.eclipse.m2e;

import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.eclipse.core.VimidePlugin;
import org.vimide.eclipse.core.service.IProjectService;

/**
 * An vimide plugin implementation of m2e supported.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimideM2ePlugin extends VimidePlugin {

    private static final String MAVEN2_NATURE = "org.eclipse.m2e.maven2Nature";
    static final Logger LOGGER = LoggerFactory.getLogger(VimideM2ePlugin.class);

    /**
     * {@inheritDoc}
     * 
     * @see org.vimide.eclipse.core.VimidePlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.vimide.eclipse.core.VimidePlugin#activate(org.osgi.framework.BundleContext)
     */
    @Override
    protected void activate(BundleContext context) {
        super.activate(context);

        boolean activateRequired = false;

        IProjectNatureDescriptor[] descriptors = ResourcesPlugin.getWorkspace()
                .getNatureDescriptors();
        if (null != descriptors && descriptors.length > 0) {
            // find m2e natures ID.
            for (IProjectNatureDescriptor desc : descriptors) {
                if (desc.getNatureId().equals(MAVEN2_NATURE)) {
                    // nature found.
                    activateRequired = true;
                    break;
                }
            }
        }

        if (activateRequired) {
            IProjectService projectService = null;
        }
    }

}
