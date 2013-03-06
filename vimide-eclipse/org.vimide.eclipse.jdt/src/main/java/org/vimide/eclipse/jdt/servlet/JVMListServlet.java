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
package org.vimide.eclipse.jdt.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Requests to list specific available JVMs.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/java_list_vms")
public class JVMListServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {
        final List<Map<String, Object>> results = Lists.newArrayList();

        final IVMInstall defaultVMInstall = JavaRuntime.getDefaultVMInstall();
        final IVMInstallType[] vmInstallTypes = JavaRuntime.getVMInstallTypes();

        for (IVMInstallType type : vmInstallTypes) {
            IVMInstall[] installs = type.getVMInstalls();
            if (installs.length > 0) {
                for (IVMInstall install : installs) {
                    Map<String, Object> result = Maps.newHashMap();
                    result.put("type", type.getName());
                    result.put("name", install.getName());
                    result.put("dir", install.getInstallLocation()
                            .getAbsolutePath());
                    result.put("id", install.getId());
                    String[] args = install.getVMArguments();
                    result.put("args",
                            args == null ? "" : Arrays.toString(args));
                    result.put("default", install.equals(defaultVMInstall) ? 1
                            : 0);
                    results.add(result);
                }
            }
        }

        // Output to response.
        resp.writeAsJson(results);
    }
}
