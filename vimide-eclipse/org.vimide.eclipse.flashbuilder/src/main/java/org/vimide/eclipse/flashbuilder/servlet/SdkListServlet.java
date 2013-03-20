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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.core.servlet.VimideHttpServletRequest;
import org.vimide.core.servlet.VimideHttpServletResponse;
import org.vimide.eclipse.core.servlet.GenericVimideHttpServlet;

import com.adobe.flexbuilder.project.internal.FlexProjectCore;
import com.adobe.flexbuilder.project.sdks.IFlexSDK;
import com.adobe.flexbuilder.project.sdks.IFlexSDKPreferences;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Requests to list all Flex SDK installed.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@WebServlet(urlPatterns = "/flexListSdks")
public class SdkListServlet extends GenericVimideHttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Just a logger.
     */
    static final Logger log = LoggerFactory.getLogger(SdkListServlet.class
            .getName());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(VimideHttpServletRequest req,
            VimideHttpServletResponse resp) throws ServletException,
            IOException {
        final List<Map<String, Object>> results = Lists.newArrayList();

        final IFlexSDKPreferences preferences = FlexProjectCore.getDefault()
                .getFlexSDKPreferences();
        final IFlexSDK defaultSdk = preferences.getDefaultFlexSDK();
        final IFlexSDK[] sdks = preferences.getItems();

        for (IFlexSDK sdk : sdks) {
            Map<String, Object> sdkEntry = Maps.newHashMap();
            sdkEntry.put("name", sdk.getName());
            sdkEntry.put("version", sdk.getVersion().toString());
            sdkEntry.put("path", sdk.getLocation().toOSString());
            sdkEntry.put("targetPlayerVersion", sdk.getTargetPlayerVersion()
                    .toString());
            sdkEntry.put("valid", sdk.isValid() ? 1 : 0);

            IStatus status = sdk.validate();
            if (!status.isOK()) {
                sdkEntry.put("error", 1);
                sdkEntry.put("message", status.getMessage());
            }

            if (sdk == defaultSdk || sdk.equals(defaultSdk)) {
                sdkEntry.put("default", 1);
            } else {
                sdkEntry.put("default", 0);
            }

            results.add(sdkEntry);
        }

        resp.writeAsJson(results);
    }

}

// vim:ft=java
