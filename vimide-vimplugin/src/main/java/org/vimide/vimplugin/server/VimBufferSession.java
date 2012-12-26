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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.session.IoSession;
import org.vimide.vimplugin.builder.VimCommandBuilder;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimBufferSession {

    private final int bufferId;
    private AtomicInteger seqno = new AtomicInteger(0);

    private final Map<String, Set<VimEventListener>> listeners = new HashMap<String, Set<VimEventListener>>();
    private static ThreadLocal<IoSession> ioSessionLocal = new ThreadLocal<IoSession>() {

        /**
         * {@inheritDoc}
         * 
         * @see java.lang.ThreadLocal#set(java.lang.Object)
         */
        @Override
        public void set(IoSession value) {
            synchronized (ioSessionLocal) {
                super.set(value);
            }
        }

    };

    /**
     * Creates an new VimBufferSession instance.
     * 
     * @param bufferId
     */
    public VimBufferSession(int bufferId) {
        super();
        this.bufferId = bufferId;
    }

    public int getBufferId() {
        return bufferId;
    }

    public int nextSeqno() {
        return seqno.getAndIncrement();
    }

    public void addEventListener(String name, VimEventListener listener) {
        if (null != name && null != listener) {
            if (!listeners.containsKey(name)) {
                listeners.put(name, new HashSet<VimEventListener>());
            }
            Set<VimEventListener> set = listeners.get(name);
            if (!set.contains(listener))
                set.add(listener);
        }
    }

    public void removeEventListener(String name, VimEventListener listener) {
        if (null != name && null != listener) {
            if (listeners.containsKey(name)) {
                Set<VimEventListener> set = listeners.get(name);
                if (null != set && !set.isEmpty()) {
                    set.remove(listener);
                }
            }
        }
    }

    public void clear() {
        listeners.clear();
        seqno.set(0);
    }

    protected void dispatch(VimEvent event) {
        if (null != event) {
            if (listeners.containsKey(event.getName())) {
                try {
                    final Set<VimEventListener> set = listeners.get(event
                            .getName());
                    if (null != set) {
                        for (VimEventListener l : set
                                .toArray(new VimEventListener[set.size()])) {
                            l.actived(event);
                        }
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public IoSession getIoSession() {
        return ioSessionLocal.get();
    }

    protected void setIoSession(IoSession session) {
        ioSessionLocal.set(session);
    }

    public VimCommandBuilder commandBuilder() {
        return new VimCommandBuilder() {

            private StringBuilder datas = new StringBuilder();

            private VimData data = new VimData(VimBufferSession.this,
                    nextSeqno(), 1);

            @Override
            public VimCommandBuilder bufferId(int bufferId) {
                data.setBufferId(bufferId);
                return this;
            }

            @Override
            public VimCommandBuilder command(String command) {
                data.setName(command);
                return this;
            }

            protected void paddingData() {
                if (datas.length() > 0) {
                    datas.append(" ");
                }
            }

            @Override
            public VimCommandBuilder booleanData(boolean data) {
                paddingData();

                if (data) {
                    datas.append("T");
                } else {
                    datas.append("F");
                }
                return this;
            }

            @Override
            public VimCommandBuilder stringData(String data) {
                if (null != data && !data.isEmpty()) {
                    paddingData();
                    datas.append("\"");
                    datas.append(data);
                    datas.append("\"");
                }

                return this;
            }

            @Override
            public VimCommandBuilder intData(int data) {
                paddingData();
                datas.append(String.valueOf(data));
                return this;
            }

            @Override
            public VimCommandBuilder axisData(int[] data) {
                if (null != data && data.length >= 2) {
                    paddingData();
                    datas.append(data[0] + "/" + data[1]);
                }
                return this;
            }

            @Override
            public VimData toCommandData() {
                data.setData(datas.toString());
                return data;
            }

            @Override
            public String toString() {
                return data.toString();
            }
        };
    }
}
