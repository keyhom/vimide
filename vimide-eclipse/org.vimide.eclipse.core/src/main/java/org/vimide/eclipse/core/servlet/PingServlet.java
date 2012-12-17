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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.VimideCorePlugin;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/ping")
public class PingServlet extends GenericVimideHttpServlet {

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
        // write the eclipse version and vimide version.

        final String eclipseVersion = getGenericVersion(Platform
                .getBundle("org.eclipse.platform"));
        final String vimideVersion = getGenericVersion(VimideCorePlugin
                .getDefault().getBundle());

        Map<String, Object> result = Maps.newHashMap();
        result.put("Eclipse", eclipseVersion);
        result.put("Vimide", vimideVersion);

        resp.writeAsJson(result).flush();
    }

    private String getGenericVersion(Bundle bundle) {
        if (null != bundle) {
            String eclipseVersion = (String) bundle.getHeaders().get(
                    "Bundle-Version");
            if (!Strings.isNullOrEmpty(eclipseVersion)) {
                eclipseVersion = eclipseVersion.replaceFirst("([0-9.]+).*",
                        "$1").replaceFirst("\\.$", "");
                return eclipseVersion;
            }
        }
        return null;
    }

}
