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
package org.vimide.vimplugin.handler;

import org.vimide.vimplugin.annotation.VimHeader;
import org.vimide.vimplugin.message.VimMessageHandler;
import org.vimide.vimplugin.server.VimResponse;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@VimHeader(name = "disconnect")
public class DisconnectHandler extends VimMessageHandler {

    /**
     * {@inheritDoc}
     * 
     * @see org.vimide.vimplugin.message.VimMessageHandler#execute(org.vimide.vimplugin.server.VimResponse)
     */
    @Override
    public void execute(VimResponse vimResponse) throws Exception {

    }

}
