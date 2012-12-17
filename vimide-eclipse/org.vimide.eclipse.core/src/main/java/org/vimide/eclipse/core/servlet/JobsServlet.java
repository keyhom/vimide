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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.vimide.core.servlet.VimideHttpServlet;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * An implementation of collecting the eclipse's job queue.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/jobs")
public class JobsServlet extends VimideHttpServlet {

    private static final long serialVersionUID = 1L;

    static final String AUTO_BUILD = "auto_build";
    static final String AUTO_REFRESH = "auto_refresh";
    static final String MANUAL_BUILD = "manual_build";
    static final String MANUAL_REFRESH = "manual_refresh";

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

        final Object family = getFamliy(req.getNotNullParameter("familly"));

        IJobManager manager = Job.getJobManager();
        Job[] jobs = manager.find(family);

        final List<Map<String, String>> results = Lists.newArrayList();

        for (Job job : jobs) {
            Map<String, String> result = Maps.newHashMap();
            result.put("job", job.toString());
            result.put("status", getStatus(job));
            results.add(result);
        }

        try {
            resp.writeAsJson(results).flush();
        } catch (final IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    Object getFamliy(String family) {
        if (!family.isEmpty()) {
            if (AUTO_BUILD.equals(family))
                return ResourcesPlugin.FAMILY_AUTO_BUILD;
            else if (AUTO_REFRESH.equals(family))
                return ResourcesPlugin.FAMILY_AUTO_REFRESH;
            else if (MANUAL_BUILD.equals(family))
                return ResourcesPlugin.FAMILY_MANUAL_BUILD;
            else if (MANUAL_REFRESH.equals(family))
                return ResourcesPlugin.FAMILY_MANUAL_REFRESH;
        }
        return null;
    }

    String getStatus(Job job) {
        int status = job.getState();
        switch (status) {
            case Job.RUNNING:
                return "running";
            case Job.SLEEPING:
                return "sleeping";
            case Job.WAITING:
                return "waiting";
            case Job.NONE:
                return "none";
        }

        return StringUtils.EMPTY;
    }
}
