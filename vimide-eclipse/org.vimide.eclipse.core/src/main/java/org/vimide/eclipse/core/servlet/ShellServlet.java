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
package org.vimide.eclipse.core.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.core.util.CommandLineExecutor;

import com.google.common.base.Strings;

/**
 * An implementation of shell execution with supplied command-line.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/shell")
public class ShellServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Logger
     */
    static final Logger LOGGER = LoggerFactory.getLogger(ShellServlet.class);

    static final int DEFAULT_TIMEOUT = 5000;

    /**
     * {@inheritDoc}
     * 
     * @see org.vimide.core.servlet.VimideHttpServlet#doGet(org.vimide.core.servlet.VimideHttpServletRequest,
     *      org.vimide.core.servlet.VimideHttpServletResponse)
     */
    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {

        final String targetEncoding = req.getNotNullParameter("encoding",
                getDefaultCharacterEncoding());

        final int timeout = req.getIntParameter("timeout", DEFAULT_TIMEOUT);
        final String command = req.getNotNullParameter("command");

        if (!command.isEmpty()) {
            try {

                CommandLineExecutor executor = CommandLineExecutor
                        .parse(command);
                executor.setWorkingDir(new File(System.getProperty("user.home")));
                executor.execute(timeout);

                if (executor.getExitCode() == 0) {
                    if (!Strings.isNullOrEmpty(targetEncoding))
                        resp.writeAsPlainText(executor
                                .getOutput(targetEncoding));
                    else
                        resp.writeAsPlainText(executor.getOutput());
                } else {
                    if (!Strings.isNullOrEmpty(targetEncoding))
                        resp.writeAsPlainText(executor.getError(targetEncoding));
                    else
                        resp.writeAsPlainText(executor.getError());

                }

            } catch (final Exception e) {
                LOGGER.error("Error caught at shell execute: {}",
                        e.getMessage(), e);
            } finally {
                resp.flush();
            }
        }
    }

}
