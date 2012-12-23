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
import java.util.Map;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimBufferSession {

    private final int bufferId;
    private int seqno = 0;

    private final Map<String, VimEventListener> listeners = new HashMap<String, VimEventListener>();

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
        return seqno++;
    }

    public void addEventListener(String name, VimEventListener listener) {
        if (null != name && null != listener)
            listeners.put(name, listener);
    }

    public void clear() {
        listeners.clear();
        seqno = 0;
    }

    protected void dispatch(VimEvent event) {
        if (null != event) {
            if (listeners.containsKey(event.getName())) {
                try {
                    listeners.get(event.getName()).actived(event);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
