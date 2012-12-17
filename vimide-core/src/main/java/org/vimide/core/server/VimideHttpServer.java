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
package org.vimide.core.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Http implementation of Vimide's server.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimideHttpServer {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(VimideHttpServer.class);

    /**
     * A singleton holder.
     * 
     * @author keyhom (keyhom.c@gmail.com)
     */
    private static class SingletonHolder {

        /**
         * Singleton object.
         */
        static final VimideHttpServer instance = new VimideHttpServer();
    }

    /**
     * Gets the singleton of {@link VimideHttpServer} by Bob's lazy mode.
     * 
     * @return singelton
     */
    public static VimideHttpServer getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Http daemon server instance.
     */
    private Server httpd;

    /**
     * Creates an new VimideHttpServer instance.
     */
    private VimideHttpServer() {
        super();
    }

    /**
     * Start the http server with the specified listening port.
     * 
     * @param port the listening port.
     * @throws Exception
     */
    public void start(int port) throws Exception {
        start("*", port);
    }

    /**
     * Starts the http server with the specified binding hostname and listening
     * port. If the host was null or provided as '*', then will be binding to
     * 0.0.0.0.
     * 
     * @param hostname the binding hostname.
     * @param port the listening port.
     * @throws Exception
     */
    public void start(String hostname, int port) throws Exception {
        if (!Strings.isNullOrEmpty(hostname) && hostname.trim().equals("*"))
            hostname = null;

        if (port <= 0 || port > 65535)
            throw new IllegalArgumentException(
                    "Illegal listening port was submit. It must during (0, 65535].");

        SocketAddress localAddress;
        if (null == hostname)
            localAddress = new InetSocketAddress(port);
        else
            localAddress = new InetSocketAddress(hostname, port);
        start(localAddress);
    }

    /**
     * Starts the http server with the specified binding local address.
     * 
     * @param localAddress the binding local address.
     * @throws Exception
     */
    public void start(SocketAddress localAddress) throws Exception {
        LOGGER.debug("Vimide http server starting request was submit.");

        if (null == localAddress)
            throw new IllegalArgumentException(
                    "Illegal binding local address was submit.");

        if (isRunning()) {
            throw new IllegalStateException(
                    "The httpd is running now, make sure to shutdown first.");
        }

        httpd = new Server((InetSocketAddress) localAddress);
        final ServletContextHandler contextHandler = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");
        contextHandler.setCompactPath(true);

        httpd.setHandler(contextHandler);
        httpd.start();

        LOGGER.info("Vimide http server started at {}.", localAddress);
    }

    /**
     * Stops the http server.
     * 
     * @throws Exception
     */
    public void stop() throws Exception {
        LOGGER.debug("Vimide http server stopping request was submit.");

        if (isRunning()) {
            httpd.stop();
            LOGGER.info("STOPED the vimide http server.");
        }
    }

    /**
     * Tells if the http server is running.
     * 
     * @return true is running, false otherwise.
     */
    public boolean isRunning() {
        if (null != httpd)
            return httpd.isRunning();
        return false;
    }

    /**
     * Registered the servlet.
     * <p>
     * NOTE: the servlet must implemented with servlet 3.0
     * </p>
     * 
     * @param servletClass the class of servlet.
     */
    public void registerServlet(Class<? extends HttpServlet> servletClass) {
        if (null != httpd
                && httpd.getHandler() instanceof ServletContextHandler) {
            final ServletContextHandler contextHandler = (ServletContextHandler) httpd
                    .getHandler();

            final WebServlet webServlet = servletClass
                    .getAnnotation(WebServlet.class);

            if (null == webServlet)
                throw new IllegalArgumentException(
                        "The submit servlet class: '"
                                + servletClass.getName()
                                + "' wasn't implemented with servlet 3.0, see @javax.servlet.annotation.WebServlet");

            if (null != webServlet.urlPatterns()
                    && webServlet.urlPatterns().length > 0) {
                for (String pathSpec : webServlet.urlPatterns()) {
                    contextHandler.addServlet(new ServletHolder(servletClass),
                            pathSpec);
                }
            }
        }
    }

}