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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.eclipse.vimplugin.preferences.VimpluginPreferenceConstants;
import org.vimide.vimplugin.server.VimBufferSession;
import org.vimide.vimplugin.server.VimEvent;
import org.vimide.vimplugin.server.VimEventListener;

import com.google.common.base.Strings;

/**
 * The management of Vim instances.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public abstract class VimInstanceManager {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(VimInstanceManager.class);

    /**
     * Singleton holder.
     * 
     * @author keyhom (keyhom.c@gmail.com)
     */
    private static class SingletonHolder {
        static final VimInstanceManager manager = new VimInstanceManager() {
        };
    }

    /**
     * Gets the singleton of VimInstanceManager.
     * 
     * @return singleton
     */
    public static VimInstanceManager getInstance() {
        return SingletonHolder.manager;
    }

    private final AtomicInteger vimIds = new AtomicInteger(1);
    private Map<Integer, VimInstance> instances = null;

    /**
     * Creates an new VimInstanceManager instance.
     */
    private VimInstanceManager() {
        super();

        instances = new HashMap<Integer, VimInstanceManager.VimInstance>();
        LOGGER.debug("VimInstanceManager instanced.");
    }

    /**
     * Gets the vim instance by the specified vimId.
     * 
     * @param vimId the specified id of vim instance.
     * @return the vim instance.
     */
    public VimInstance getVimInstance(int vimId) {
        return instances.get(vimId);
    }

    /**
     * Gets the default vim instance, general it's the first vim instance.
     * 
     * @return the default vim instance.
     */
    public VimInstance getDefaultVimInstance() {
        return getVimInstance(1);
    }

    /**
     * Creates a new vim instance.
     * 
     * @return the new vim instance.
     */
    public VimInstance createVimInstance() {
        VimInstance instance = new VimInstance(vimIds.getAndIncrement());
        instances.put(instance.getId(), instance);
        return instance;
    }

    /**
     * Removes the vim instance by the specified vimId.
     * 
     * @param vimId the id of vim instance.
     * @return vim instance reference.
     */
    public VimInstance removeVimInstance(int vimId) {
        return instances.remove(vimId);
    }

    /**
     * Vim instance model.
     * 
     * @author keyhom (keyhom.c@gmail.com)
     */
    public class VimInstance {

        /**
         * Storage for the id.
         */
        private final int vimId;
        private Process process;
        private boolean embedded;
        private IoSession ioSession;

        /**
         * Creates an new VimInstance instance.
         * 
         * @param vimId
         */
        VimInstance(int vimId) {
            super();

            this.vimId = vimId;
        }

        /**
         * Gets the id binding with the vim instance.
         * 
         * @return id.
         */
        public int getId() {
            return vimId;
        }

        public VimInstance start(String workingDir, String filePath) {
            return this;
        }

        public VimInstance start(String workingDir, long wid) {
            String gvim = VimideVimpluginPlugin.getDefault()
                    .getPreferenceStore()
                    .getString(VimpluginPreferenceConstants.P_GVIM);
            int port = VimideVimpluginPlugin.getDefault().getPreferenceStore()
                    .getInt(VimpluginPreferenceConstants.P_PORT);
            String addoptString = VimideVimpluginPlugin.getDefault()
                    .getPreferenceStore()
                    .getString(VimpluginPreferenceConstants.P_OPTS);

            List<String> args = new ArrayList<String>();
            args.add(gvim);
            args.add("--servername");
            args.add(String.valueOf(getId()));
            args.add("-nb::" + port);
            args.add("-f");

            if (Platform.getOS().equals(Platform.OS_WIN32)) {
                args.add("--windowid");
            } else {
                args.add("--socketid");
            }
            args.add(String.valueOf(wid));

            // args.add("--cmd");
            // args.add("echo hello");

            if (!Strings.isNullOrEmpty(addoptString)) {
                for (String s : StringUtils.split(addoptString, ' ')) {
                    args.add(s);
                }
            }

            return start(workingDir, true, false,
                    args.toArray(new String[args.size()]));
        }

        protected VimInstance start(String workingDir, boolean embedded,
                boolean tabbed, String... args) {
            ProcessBuilder pb = new ProcessBuilder(args);
            try {
                if (null != workingDir && !workingDir.isEmpty()) {
                    pb.directory(new File(workingDir));
                }
                process = pb.start();
                LOGGER.debug("Vim instance #{} startup.", getId());
            } catch (final Exception e) {
                LOGGER.error("Vim instance startup error: {}", e.getMessage(),
                        e);
            }

            this.embedded = embedded;
            return this;
        }

        public void dispose() {
            VimInstanceManager.this.removeVimInstance(getId());

            if (null != process) {
                process.destroy();
                process = null;
            }

            IoSession session = getIoSession();
            if (null != session) {
                session.close(false);
            }
        }

        public boolean isEmbedded() {
            return embedded;
        }

        public void invokeAtStartup(VimEventListener listener) {
            if (null != listener) {
                final VimBufferSession defaultSession = VimideVimpluginPlugin
                        .getDefault().getVimServer().getDefaultSession();

                defaultSession.addEventListener("startupDone",
                        new VimEventListener() {

                            @Override
                            public void actived(VimEvent event)
                                    throws Exception {
                                ioSession = event.getSession().getIoSession();
                                defaultSession.removeEventListener(
                                        "startupDone", this);
                            }
                        });

                defaultSession.addEventListener("startupDone", listener);
            }
        }

        protected IoSession getIoSession() {
            return ioSession;
        }

    }

}
