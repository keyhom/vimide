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
package org.vimide.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

/**
 * Runs an external process.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class CommandExecutor implements Runnable {

    /**
     * Executes the supplied command.
     * 
     * @param cmd the command to execute.
     * @return the command exeuctor instance containing the ending state of the
     *         process.
     * @throws Exception
     */
    public static CommandExecutor execute(String[] cmd) throws Exception {
        return execute(cmd, -1);
    }

    /**
     * Executes the supplied command.
     * 
     * @param cmd the command to execute.
     * @param timeout timeout in milliseconds.
     * @return the command executor instance containing the ending state of the
     *         process.
     * @throws Exception
     */
    public static CommandExecutor execute(String[] cmd, long timeout)
            throws Exception {
        CommandExecutor executor = new CommandExecutor(cmd);

        Thread thread = new Thread(executor);
        thread.start();

        if (timeout > 0) {
            thread.join(timeout);
        } else {
            thread.join();
        }

        return executor;
    }

    private int returnCode = -1;
    private String[] cmd;
    private String result;
    private String error;
    private Process process;

    /**
     * Creates an new CommandExecutor instance.
     * 
     * @param cmd the commands.
     */
    private CommandExecutor(String[] cmd) {
        this.cmd = cmd;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec(cmd);

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ByteArrayOutputStream err = new ByteArrayOutputStream();

            Thread outThread = new Thread() {

                @Override
                public void run() {
                    try {
                        IOUtils.copy(process.getInputStream(), out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            };

            outThread.start();

            Thread errThread = new Thread() {
                @Override
                public void run() {
                    try {
                        IOUtils.copy(process.getErrorStream(), err);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            errThread.start();

            returnCode = process.waitFor();
            outThread.join(1000);
            errThread.join(1000);

            result = out.toString();
            error = err.toString();
        } catch (final Exception e) {
            returnCode = 12;
            error = e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Destroy this process.
     */
    public void destory() {
        if (process != null) {
            process.destroy();
        }
    }

    /**
     * Gets the output of the command.
     * 
     * @return the command result.
     */
    public String getResult() {
        return result;
    }

    /**
     * Gets the exit code from the process.
     * 
     * @return the exit code.
     */
    public int getExitCode() {
        return returnCode;
    }

    /**
     * Gets the error message from the command if there was one.
     * 
     * @return the possibly empty error message.
     */
    public String getErrorMessage() {
        return error;
    }
}
