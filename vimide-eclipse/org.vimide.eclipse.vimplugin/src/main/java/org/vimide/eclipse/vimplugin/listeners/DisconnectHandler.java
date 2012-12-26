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
package org.vimide.eclipse.vimplugin.listeners;

import org.vimide.vimplugin.server.VimEvent;
import org.vimide.vimplugin.server.VimEventListener;

/**
 * Handles with the vim instance disconnect.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class DisconnectHandler implements VimEventListener {

    public static final String DISCONNECT = "disconnect";
    public static final String KILLED = "killed";

    /**
     * {@inheritDoc}
     * 
     * @see org.vimide.vimplugin.server.VimEventListener#actived(org.vimide.vimplugin.server.VimEvent)
     */
    @Override
    public void actived(VimEvent event) throws Exception {
        if (event.getName().equals(DISCONNECT)
                || event.getName().equals(KILLED)) {
        }
    }

}
