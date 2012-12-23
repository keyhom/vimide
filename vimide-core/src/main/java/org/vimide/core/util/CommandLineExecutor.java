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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Runs a command-line in external process.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class CommandLineExecutor {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommandLineExecutor.class);

    /**
     * Parses the supplied <code>commandLine</code> to an command-line executor.
     * 
     * @param commandLine the command-line value.
     * @return an executor container.
     */
    public static CommandLineExecutor parse(String commandLine) {
        if (Strings.isNullOrEmpty(commandLine))
            throw new IllegalArgumentException("Illegal commandLine argument.");
        return new CommandLineExecutor(commandLine);
    }

    /**
     * Parses the supplied <code>executable</code> to an command-line executor.
     * 
     * @param executable the executable file.
     * @return an executor container.
     */
    public static CommandLineExecutor parse(File executable) {
        if (null == executable || !executable.exists()) {
            throw new IllegalArgumentException("Illegal executable file.");
        }
        return new CommandLineExecutor(executable);
    }

    /**
     * Parses the supplied <code>executable</code> and <code>options</code> to
     * an command-line executor.
     * 
     * @param executable the executable program.
     * @param options the options with the executable.
     * @return an executor container.
     */
    public static CommandLineExecutor parse(String executable,
            String... options) {
        if (Strings.isNullOrEmpty(executable))
            throw new IllegalArgumentException(
                    "Illegal executable was supplied.");
        return new CommandLineExecutor(executable, options);
    }

    /**
     * Parses the supplied <code>commands</code> to an command-line executor.
     * 
     * @param commands the command-line values.
     * @return an executor container.
     */
    public static CommandLineExecutor parse(String... commands) {
        if (null == commands || commands.length == 0)
            throw new IllegalArgumentException(
                    "Illegal command-line arguments.");
        return new CommandLineExecutor(commands);
    }

    private final CommandLine cl;
    private int exitCode = -1;
    private byte[] output = new byte[] {};
    private byte[] error = new byte[] {};
    private File workingDir;

    /**
     * Creates an new CommandLineExecutor instance.
     * 
     * @param commandLine the command-line value.
     */
    private CommandLineExecutor(String commandLine) {
        super();

        cl = CommandLine.parse(commandLine);
    }

    /**
     * Creates an new CommandLineExecutor instance.
     * 
     * @param executable the executable file.
     */
    private CommandLineExecutor(File executable) {
        super();

        cl = new CommandLine(executable);
    }

    /**
     * Creates an new CommandLineExecutor instance.
     * 
     * @param executable the executable value.
     * @param options the options.
     */
    private CommandLineExecutor(String executable, String... options) {
        super();

        cl = new CommandLine(executable);
        if (null != options && options.length > 0) {
            cl.addArguments(options);
        }
    }

    /**
     * Creates an new CommandLineExecutor instance.
     * 
     * @param commands
     */
    private CommandLineExecutor(String[] commands) {
        super();

        cl = new CommandLine(commands[0]);

        for (int i = 1; i < commands.length; i++) {
            cl.addArgument(commands[i]);
        }
    }

    /**
     * Executes the command line.
     * 
     * @return the reference pass to this.
     */
    public CommandLineExecutor execute() {
        return execute(-1);
    }

    /**
     * Executes the command line.
     * 
     * @param timeout the execute timeout.
     * @return the reference pass to this.
     */
    public CommandLineExecutor execute(long timeout) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();

        final Executor exec = new DefaultExecutor();
        try {
            exec.setStreamHandler(new PumpStreamHandler(outStream, errStream));

            if (timeout > 0) {
                ExecuteWatchdog wd = new ExecuteWatchdog(timeout);
                exec.setWatchdog(wd);
            }

            if (null != workingDir && workingDir.exists()) {
                exec.setWorkingDirectory(workingDir);
            }

            this.exitCode = exec.execute(cl, System.getenv());
            output = outStream.toByteArray();
        } catch (final Exception e) {
            error = errStream.toByteArray();
            LOGGER.error("Execute command faild: {}", cl, e);
        } finally {
            IOUtils.closeQuietly(outStream);
            IOUtils.closeQuietly(errStream);
        }

        return this;
    }

    protected String getPlatfomrOsCharsetEncoding() {
        String property = System.getProperty("sun.jnu.encoding");
        if (Strings.isNullOrEmpty(property)) {
            property = Charset.defaultCharset().name();
        }
        return property;
    }

    public void setWorkingDir(File file) {
        this.workingDir = file;
    }

    /**
     * Gets the exit code by command executed.
     * 
     * @return the exit code, 0 is normal exit, otherwise maybe exception exit.
     */
    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        try {
            return new String(output, getPlatfomrOsCharsetEncoding());
        } catch (UnsupportedEncodingException ignore) {
        }
        return null;
    }

    public String getOutput(String encoding) {
        if (!Strings.isNullOrEmpty(encoding)) {
            try {
                return new String(output, encoding);
            } catch (final UnsupportedEncodingException ignore) {
            }
        } else {
            return getOutput();
        }

        return null;
    }

    public String getError() {
        try {
            return new String(error, getPlatfomrOsCharsetEncoding());
        } catch (UnsupportedEncodingException ignore) {
        }
        return null;
    }

    public String getError(String encoding) {
        if (!Strings.isNullOrEmpty(encoding)) {
            try {
                return new String(error, encoding);
            } catch (final UnsupportedEncodingException ignore) {
            }
        } else {
            return getError();
        }
        return null;
    }

}
