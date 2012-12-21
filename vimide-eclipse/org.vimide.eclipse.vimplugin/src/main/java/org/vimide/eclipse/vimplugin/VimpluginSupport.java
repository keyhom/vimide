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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.util.CommandExecutor;
import org.vimide.eclipse.vimplugin.preferences.VimpluginPreferenceConstants;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * Support for vimplugin.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimpluginSupport {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(VimpluginSupport.class);

    private static final String GVIM_FEATURE_TEST = "redir! > <file> "
            + "| silent! <command> " + "| quit";
    private static final String GVIM_FEATURE_COMMAND_UNIX = "echo 'embed:' . "
            + "(v:version >= 700 && has('gui_gtk')) . "
            + "' netbeans:' . (has('netbeans_intg')) . "
            + "' netbeansDocumentListen:' . "
            + "(v:version > 702 || (v:version == 702 && has('patch359')))";
    private static final String GVIM_FEATURE_COMMAND_WINDOWS = "echo 'embed:' . "
            + "(v:version > 701 || (v:version == 701 && has('patch091'))) . "
            + "' netbeans:' . (has('netbeans_intg')) . "
            + "' netbeansDocumentListen:' . "
            + "(v:version > 702 || (v:version == 702 && has('patch359')))";

    private static Map<String, Boolean> features = Maps.newHashMap();

    /**
     * Creates an new VimpluginSupport instance.
     */
    public VimpluginSupport() {
        super();
    }

    /**
     * Determines if the configured gvim path availabe.
     * 
     * @return true if the configured gvim path availabe, false otherwise.
     */
    public boolean isGvimAvailable() {
        if (null != features
                && features.containsKey(VimpluginPreferenceConstants.P_GVIM)) {
            return features.get(VimpluginPreferenceConstants.P_GVIM);
        }

        boolean available = false;

        String gvim = VimideVimpluginPlugin.getDefault().getPreferenceStore()
                .getString(VimpluginPreferenceConstants.P_GVIM);

        if (!Strings.isNullOrEmpty(gvim)) {

            // the configured gvim just the executable gvim, not the absolute
            // path.
            // determines the configured gvim path running in command line.

            try {

                final File tempFile = File.createTempFile("vimide_gvim", null);
                tempFile.deleteOnExit();

                CommandExecutor ce = CommandExecutor.execute(new String[] {
                        gvim,
                        "-f",
                        "-X",
                        "-u",
                        "NONE",
                        "-U",
                        "NONE",
                        "--cmd",
                        "redir! > " + tempFile.getAbsolutePath()
                                + " | silent! echo version | quit" }, 5000);

                FileInputStream fis = new FileInputStream(tempFile);
                try {
                    if (ce.getExitCode() == 0) {
                        // command execute successfully.
                        String result = IOUtils.toString(fis);
                        LOGGER.debug("Gvim test output: {}", result);
                        if (!Strings.isNullOrEmpty(result)) {
                            try {
                                int version = Integer.parseInt(result.trim());
                                if (version >= 700) {
                                    available = true;
                                    LOGGER.debug("Gvim is available.");
                                }
                            } catch (NumberFormatException ignore) {
                            }
                        }
                    }
                } finally {
                    IOUtils.closeQuietly(fis);
                    try {
                        tempFile.delete();
                    } catch (final Exception ignore) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        features.put(VimpluginPreferenceConstants.P_GVIM, available);
        if (!available) {
            LOGGER.debug("Gvim is unavailable.");
        }
        return available;
    }

    /**
     * Determines if the gvim has the specified feature supported.
     * 
     * @param feature the specified feature.
     * @return true if the gvim supported the specified feature, false
     *         otherwise.
     */
    public boolean hasFeature(String feature) {

        if (null != features && features.containsKey(feature)) {
            return features.get(feature);
        }

        String gvim = VimideVimpluginPlugin.getDefault().getPreferenceStore()
                .getString(VimpluginPreferenceConstants.P_GVIM);

        try {
            File tempFile = File.createTempFile("vimide_gvim", null);
            tempFile.deleteOnExit();
            String command = GVIM_FEATURE_COMMAND_UNIX;
            if (Platform.getOS().equals(Platform.OS_WIN32)) {
                command = GVIM_FEATURE_COMMAND_WINDOWS;
            }

            command = GVIM_FEATURE_TEST.replaceFirst("<command>", command);
            command = command
                    .replaceFirst("<file>", tempFile.getAbsolutePath());

            String[] cmd = new String[] { gvim, "-f", "-X", "-u", "NONE", "-U",
                    "NONE", "--cmd", command };

            final CommandExecutor ce = CommandExecutor.execute(cmd, 5000);

            int exitCode = ce.getExitCode();
            String errorMessage = ce.getErrorMessage();

            try {
                if (exitCode != 0) {
                    LOGGER.error("Failed to execute gvim: {}", errorMessage);
                    return false;
                }

                FileInputStream in = null;

                try {
                    String result = IOUtils.toString(
                            in = new FileInputStream(tempFile)).trim();

                    LOGGER.debug("Gvim features supported: {}", result);

                    features.clear();

                    for (String f : StringUtils.split(result)) {
                        String[] keyVal = StringUtils.split(f, ":");
                        if (keyVal.length != 2) {
                            LOGGER.error("Invalid response from gvim: {}",
                                    result);
                            return false;
                        }
                        features.put(keyVal[0], keyVal[1].trim().equals("1"));
                    }

                } catch (IOException e) {
                    LOGGER.error("Unable to read temp file.", e);
                    return false;
                } finally {
                    IOUtils.closeQuietly(in);
                    try {
                        tempFile.delete();
                    } catch (Exception ignore) {
                    }
                }

            } finally {
            }

        } catch (final Exception e) {
            LOGGER.error("Unable to execute gvim.", e);
            return false;
        }

        return features.containsKey(feature)
                && features.get(feature).booleanValue();
    }

    /**
     * Determines if the configured gvim instance supports embedding.
     * 
     * @return true if embedding is suppported, false otherwise.
     */
    public boolean isEmbedSupported() {
        return hasFeature("embed");
    }

    /**
     * Determines if the configured gvim instance supports the required netbeans
     * interface.
     * 
     * @return true if netbeans supported, false otherwise.
     */
    public boolean isNbSupported() {
        return hasFeature("netbeans");
    }

    /**
     * Determines if the configured gvim can <a href="">reliably</a> support the
     * netbeans document listening events.
     * 
     * @return true if document listening is reliably supported, false
     *         otherwise.
     */
    public boolean isNbDocumentListenSupported() {
        return hasFeature("netbeansDocumentListen");
    }

    /**
     * Resets the features cache.
     */
    public void reset() {
        features.clear();
    }

}
