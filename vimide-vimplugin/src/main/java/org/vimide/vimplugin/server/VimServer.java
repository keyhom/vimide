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
package org.vimide.vimplugin.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.IoUtil;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineDecoder;
import org.apache.mina.filter.codec.textline.TextLineEncoder;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a server of vim protocol with netbeans.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimServer {

    /**
     * Logger
     */
    static final Logger LOGGER = LoggerFactory.getLogger(VimServer.class);

    /**
     * Storages servers of vim.
     */
    static final Map<Integer, VimServer> servers = new HashMap<Integer, VimServer>();

    /**
     * Gets the VimServer by the specified id.
     * 
     * @param serverId the id of VimServer.
     * @return the server of vim.
     */
    public static VimServer getServer(Integer serverId) {
        if (!servers.containsKey(serverId))
            servers.put(serverId, new VimServer(serverId));
        return servers.get(serverId);
    }

    private int seqno = 0;
    private int bufId = 1;
    Map<Integer, String> buffers = new HashMap<Integer, String>();

    private int serverId;
    private SocketAcceptor acceptor;
    private final Map<Integer, VimBufferSession> sessions;

    /**
     * Creates an new VimServer instance.
     */
    private VimServer(int serverId) {
        super();

        this.serverId = serverId;
        this.sessions = new HashMap<Integer, VimBufferSession>();
    }

    /**
     * Gets the id of server.
     * 
     * @return server id.
     */
    public int getServerId() {
        return serverId;
    }

    /**
     * Starts the vim server.
     * 
     * @param localAddress the local address to listen.
     */
    public void start(SocketAddress localAddress) {
        LOGGER.debug("Starts the vim server: {}", this);
        try {
            final NioSocketAcceptor acceptor = new NioSocketAcceptor(Runtime
                    .getRuntime().availableProcessors() + 1);
            final DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
            chain.addLast("codec", new ProtocolCodecFilter(
                    new TextLineEncoder(), new TextLineDecoder()));
            chain.addLast("logging", new LoggingFilter());
            chain.addLast("executor", new ExecutorFilter(Runtime.getRuntime()
                    .availableProcessors(), Runtime.getRuntime()
                    .availableProcessors() * 2));
            acceptor.setHandler(new VimEventHandler(this));

            this.acceptor = acceptor;
            acceptor.bind(localAddress);
        } catch (final Exception e) {
            LOGGER.error("Starting the vim server failed: {}", e.getMessage(),
                    e);
        } finally {
            if (!sessions.containsKey("0")) {
                // add default empty VimBufferSession for bufId = 0
                sessions.put(0, new VimBufferSession(0) {

                    /**
                     * {@inheritDoc}
                     * 
                     * @see org.vimide.vimplugin.server.VimBufferSession#dispatch(org.vimide.vimplugin.server.VimEvent)
                     */
                    @Override
                    protected void dispatch(VimEvent event) {
                        // nothing dispatch.
                    }
                });
            }
        }
    }

    /**
     * Stops the vim server.
     */
    public void stop() {
        if (null != acceptor) {
            acceptor.unbind();
            acceptor.dispose(true);
            acceptor = null;
            LOGGER.debug("Stops the vim server : {}", this);
        }
    }

    public synchronized int nextSeqno() {
        return ++seqno;
    }

    public synchronized int nextBufId() {
        return ++bufId;
    }

    /**
     * @param content
     */
    public void broadcast(String content) {
        IoUtil.broadcast(content, acceptor.getManagedSessions().values());
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "VimServer #" + getServerId();
    }

    /**
     * Processes with the vim event.
     * 
     * @author keyhom (keyhom.c@gmail.com)
     */
    static class VimEventHandler extends IoHandlerAdapter {

        /**
         * Storages with server.
         */
        private VimServer server;

        /**
         * Creates an new VimEventHandler instance.
         * 
         * @param server the vim server.
         */
        public VimEventHandler(VimServer server) {
            super();

            this.server = server;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.apache.mina.core.service.IoHandlerAdapter#exceptionCaught(org.apache.mina.core.session.IoSession,
         *      java.lang.Throwable)
         */
        @Override
        public void exceptionCaught(IoSession session, Throwable cause)
                throws Exception {
            if (!(cause instanceof IOException))
                LOGGER.error("Exception at {}: {}", server, cause.getMessage());
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.apache.mina.core.service.IoHandlerAdapter#sessionCreated(org.apache.mina.core.session.IoSession)
         */
        @Override
        public void sessionCreated(IoSession session) throws Exception {
            LOGGER.info("{} connection established.", server);
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.apache.mina.core.service.IoHandlerAdapter#messageReceived(org.apache.mina.core.session.IoSession,
         *      java.lang.Object)
         */
        @Override
        public void messageReceived(IoSession session, Object message)
                throws Exception {
            LOGGER.debug("Message recieved at {}. ", server);
            if (message instanceof String) {
                String msg = (String) message;

                if (!msg.isEmpty()) {

                    if (msg.startsWith("AUTH")) {
                        // ignore.
                        return;
                    }

                    int bufId = 0;
                    String name = null;
                    int seqno = 0;
                    String data = null;

                    if (-1 != msg.indexOf(":") && -1 != msg.indexOf("=")) {
                        String[] strs = msg.split(" ", 2);
                        String header = strs[0];

                        String[] headers = header.split(":|=|\\s");

                        if (headers.length >= 3) {
                            bufId = Integer.parseInt(headers[0]);
                            name = headers[1].trim();
                            seqno = Integer.parseInt(headers[2]);
                        }

                        if (strs.length > 1) {
                            data = strs[1].trim();
                        }
                    }

                    if (null != name && !name.isEmpty()) {
                        // lookup in server handlers and invoke.
                        VimEvent vimEvent = new VimEvent(bufId, name, seqno,
                                data);

                        if (server.sessions.containsKey(bufId)) {
                            server.sessions.get(bufId).dispatch(vimEvent);
                        }
                    }
                }
            }
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.apache.mina.core.service.IoHandlerAdapter#sessionClosed(org.apache.mina.core.session.IoSession)
         */
        @Override
        public void sessionClosed(IoSession session) throws Exception {
            LOGGER.info("{} connection closed.", server);
        }

    }

}
