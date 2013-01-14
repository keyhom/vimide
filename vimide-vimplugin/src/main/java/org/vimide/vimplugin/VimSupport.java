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
package org.vimide.vimplugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Determines the features supported in gvim.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimSupport {

    /**
     * Logger
     */
    static final Logger LOGGER = LoggerFactory.getLogger(VimSupport.class);

    private final String executable;

    private Map<String, Boolean> features;

    /**
     * Creates an new VimSupport instance.
     */
    public VimSupport(String executable) {
        super();
        this.executable = executable;
    }

    /**
     * Determines the <code>executable</code> is an available gvim.
     * 
     * @return true if the supplied executable is a gvim, false otherwise.
     */
    public boolean isAvailableExecutable() {
        if (null != executable && !executable.isEmpty()) {
            String version = VimExecutor.execute(executable, "echo version");
            if (null != version && !version.isEmpty()) {
                try {
                    if (Integer.parseInt(version.trim()) >= 700) {
                        return true;
                    }
                } catch (final NumberFormatException ignore) {
                }
            }
        }
        return false;
    }

    /**
     * Determines the supplied <code>executable</code> is an available gvim.
     * 
     * @param executable the executable command or file path.
     * @return true if the supplied is a gvim, false otherwise.
     */
    public static boolean isAvailableExecutable(String executable) {
        return new VimSupport(executable).isAvailableExecutable();
    }

    /**
     * Determines the supplied file is an executable gvim.
     * 
     * @param executable the executable file.
     * @return true if the supplied file is a gvim, false otherwise.
     */
    public static boolean isAvailableExecutable(File executable) {
        if (null != executable && executable.exists()) {
            return new VimSupport(executable.getAbsolutePath())
                    .isAvailableExecutable();
        }

        throw new IllegalArgumentException("Illegal executable file.");
    }

    /**
     * Gets the executable gvim path or command.
     * 
     * @return the executable gvim.
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Determines if the gvim the embed supported.
     * 
     * @return true if the gvim supporting the embed mode, false otherwise.
     */
    public boolean isEmbedEnabled() {
        if (null == features)
            parse();
        return features.get("embed");
    }

    /**
     * Determines if the gvim the netbeans enabled.
     * 
     * @return true if the gvim contains the netbeans features, false otherwise.
     */
    public boolean isNbEnabled() {
        if (null == features) {
            parse();
        }
        return features.get("netbeans");
    }

    /**
     * Determines if the gvim the netbeans document listen enabled.
     * 
     * @return true if the gvim contains the netbeans document listen features,
     *         false otherwise.
     */
    public boolean isNbDocumentListenEnabled() {
        if (null == features) {
            parse();
        }

        return features.get("netbeansDocumentListen");
    }

    /**
     * Parses the executable and fetch the features.
     */
    private void parse() {
        String commandFileName = null;
        if (System.getProperty("os.name").contains("Windows")) {
            commandFileName = "command_win32.properties";
        } else if (System.getProperty("os.name").contains("Linux")
                || System.getProperty("os.name").contains("Unix")) {
            commandFileName = "command_linux.properties";
        }
        try {
            Properties props = new Properties();
            props.load(this.getClass().getResourceAsStream(
                    "/" + commandFileName));
            String command = (String) props.get("vim.features.command");
            if (null == command || command.isEmpty())
                return;
            
            String result = VimExecutor.execute(getExecutable(), command);
            if (null != result && !result.isEmpty()) {
                features = new HashMap<String, Boolean>();
                final StringTokenizer st = new StringTokenizer(result, " ");
                while (st.hasMoreTokens()) {
                    String value = st.nextToken();
                    if (null != value && !value.isEmpty()) {
                        String[] keyVal = value.split(":");
                        if (keyVal.length == 2) {
                            features.put(keyVal[0].trim(), keyVal[1].trim()
                                    .equals("1") ? true : false);
                        }
                    }
                }
            }
        } catch (final IOException ignore) {
            ignore.printStackTrace();
        }
    }
}

class VimExecutor {

    static final String TEMP_PREFIX = UUID.randomUUID().toString();

    public static File getTempFile() throws Exception {
        File file = File.createTempFile(TEMP_PREFIX, null);
        file.deleteOnExit();
        return file;
    }

    public static String execute(String execute, String command) {
        File tempFile = null;
        try {
            tempFile = getTempFile();
            String[] cmdarray = new String[] {
                    execute,
                    "-u",
                    "NONE",
                    "-U",
                    "NONE",
                    "--cmd",
                    "redir! > " + tempFile.getAbsolutePath() + " | silent! "
                            + command + " | redir! END | quit" };
            final Process process = Runtime.getRuntime().exec(cmdarray);
            if (process.waitFor() == 0) {
                return FileUtils.readFileToString(tempFile);
            }
        } catch (final Exception ignore) {
        } finally {
            if (null != tempFile)
                tempFile.delete();
        }
        return null;
    }

}