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

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.session.IoSession;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimResponse {

    public static final int CMD = 1;
    public static final int FUN = 2;

    private final IoSession session;

    /**
     * Creates an new VimResponse instance.
     * 
     * @param session
     */
    public VimResponse(IoSession session) {
        super();
        this.session = session;
    }

    public int nextBufferId() {
        synchronized (session) {
            return nextSessionValue("bufferId");
        }
    }

    public int nextSeqno() {
        return nextSessionValue("seqno");
    }

    private int nextSessionValue(String key) {
        Object attribute = session.getAttribute(key, 1);

        if (attribute instanceof Integer) {
            int value = ((Integer) attribute).intValue();
            session.setAttribute(key, ++value);
            return value;
        }

        return 0;
    }

    /**
     * @param message
     */
    public void write(Object message) {
        getSession().write(message);
    }

    /**
     * @return
     */
    private IoSession getSession() {
        return session;
    }

    /**
     * Disconnect to vim editor.
     */
    public void disconnect() {
        getSession().close(false);
    }

    public ResponseBuilder getResponseBuilder(int type) {
        return new ResponseBuilder(type);
    }

    public class ResponseBuilder {

        private int bufferId;
        private String name;
        private List<String> data;
        private final int type;

        /**
         * Creates an new CmdBuilder instance.
         * 
         * @param server
         */
        public ResponseBuilder(int type) {
            this.type = type;
            data = new ArrayList<String>();
        }

        public ResponseBuilder appendBufferId(int bufferId) {
            this.bufferId = bufferId;
            return this;
        }

        public ResponseBuilder appendCommand(String command) {
            this.name = command;
            return this;
        }

        public ResponseBuilder appendData(Object data) {
            if (null != data) {
                if (data instanceof String) {
                    this.data.add("\"" + data + "\"");
                } else if (data instanceof Boolean) {
                    this.data.add(Boolean.valueOf(data.toString()) ? "T" : "F");
                } else if (data instanceof Integer) {
                    this.data.add(String.valueOf(data));
                } else if (data instanceof int[]) {
                    String t = "";
                    for (int a : (int[]) data) {
                        if (t.length() > 0)
                            t += "/";
                        t += a;
                    }
                    this.data.add(t);
                }
            }
            return this;
        }

        public String toCommand() {
            String s = this.type == 1 ? "!" : "/";
            StringBuilder sb = new StringBuilder();
            sb.append(bufferId).append(":");
            sb.append(name);
            sb.append(s);
            sb.append(nextSeqno());

            for (String ss : data) {
                sb.append(" ");
                sb.append(ss);
            }
            return sb.toString();
        }

        public String toString() {
            return toCommand();
        }

    }

}
