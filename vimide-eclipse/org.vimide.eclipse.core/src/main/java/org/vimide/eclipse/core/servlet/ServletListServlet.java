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

import java.io.IOException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.vimide.core.server.VimideHttpServer;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;

/**
 * Servlet to show the defined serve request handlers.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/servletList")
@Deprecated
public class ServletListServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

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

        final List<Class<? extends Servlet>> servlets = VimideHttpServer
                .getInstance().listServlets();

        resp.writeAsHtml("<h1>Servlet serving list:</h1>");

        if (null != servlets && !servlets.isEmpty()) {
            resp.writeAsHtml("<ul>");
            for (Class<? extends Servlet> cla : servlets) {
                WebServlet annotation = cla.getAnnotation(WebServlet.class);
                if (null != annotation && annotation.urlPatterns().length > 0) {
                    resp.writeAsHtml("<li>");
                    resp.writeAsHtml("<a href=\"" + annotation.urlPatterns()[0]
                            + "\" target=\"_blank\">" + cla.getSimpleName() + "</a>");
                    resp.writeAsHtml("</li>");
                }
            }
            resp.writeAsHtml("</ul>");
        } else {
            resp.writeAsHtml("<em>No servlets in serving.</em>");
        }

        resp.flush();
    }

}
