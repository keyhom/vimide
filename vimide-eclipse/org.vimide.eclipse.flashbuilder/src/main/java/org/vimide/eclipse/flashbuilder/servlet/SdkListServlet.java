/**
 * Copyright (c) 2013 keyhom.c@gmail.com.
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
package org.vimide.eclipse.flashbuilder.servlet;

import java.io.IOException;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

/**
 * Requests to list all Flex SDK installed.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class SdkListServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Just a logger.
     */
    static final Logger log = LoggerFactory.getLogger(SdkListServlet.class
            .getName());

    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {
        System.out.println("SdkListServlet");
        Object result = null;

        if (null == result)
            result = 1;
        resp.writeAsJson(result);
    }

}

// vim:ft=java
