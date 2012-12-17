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
package org.vimide.core.server.filter;

import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;

/**
 * Logging filter implemetation of Vimide server.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimideLoggingFilter extends LoggingFilter {

    /**
     * Creates an new VimideLoggingFilter instance.
     */
    public VimideLoggingFilter() {
        super();
    }

    /**
     * Creates an new VimideLoggingFilter instance.
     * 
     * @param name the name of filter.
     */
    public VimideLoggingFilter(String name) {
        super(name);
    }

    /**
     * Creates an new VimideLoggingFilter instance.
     * 
     * @param clazz
     */
    public VimideLoggingFilter(Class<?> clazz) {
        super(clazz);
    }

    /**
     * Creates an new VimideLoggingFilter instance.
     * 
     * @param levels the log level identity array.
     */
    public VimideLoggingFilter(String[] levels) {
        super();
        resolveLevelIdentify(levels);
    }

    /**
     * Creates an new VimideLoggingFilter instance.
     * 
     * @param name the name of filter.
     * @param levels the log level identity array.
     */
    public VimideLoggingFilter(String name, String[] levels) {
        super(name);
        resolveLevelIdentify(levels);
    }

    /**
     * Creates an new VimideLoggingFilter instance.
     * 
     * @param clazz
     * @param levels the log level identity array.
     */
    public VimideLoggingFilter(Class<?> clazz, String[] levels) {
        super(clazz);
        resolveLevelIdentify(levels);
    }

    /**
     * Resolves the identity of the log levels.
     * 
     * @param levels the log levels.
     */
    private void resolveLevelIdentify(String[] levels) {
        if (null != levels && levels.length > 0) {
            setSessionCreatedLogLevel(LogLevel.valueOf(levels[0]));
            if (levels.length > 1)
                setSessionOpenedLogLevel(LogLevel.valueOf(levels[1]));
            if (levels.length > 2)
                setSessionIdleLogLevel(LogLevel.valueOf(levels[2]));
            if (levels.length > 3)
                setSessionClosedLogLevel(LogLevel.valueOf(levels[3]));
            if (levels.length > 4)
                setExceptionCaughtLogLevel(LogLevel.valueOf(levels[4]));
            if (levels.length > 5)
                setMessageReceivedLogLevel(LogLevel.valueOf(levels[5]));
            if (levels.length >= 6)
                setMessageSentLogLevel(LogLevel.valueOf(levels[6]));
        }
    }
}
