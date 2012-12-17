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
package org.vimide.core.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of HttpServletResponse for Vimide.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimideHttpServletResponse extends HttpServletResponseWrapper {

    /**
     * Logger
     */
    static final Logger LOGGER = LoggerFactory
            .getLogger(VimideHttpServletResponse.class);
    final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates an new VimideHttpServletResponse instance.
     * 
     * @param response
     */
    public VimideHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    public void flush() throws IOException {
        getWriter().flush();
    }

    public VimideHttpServletResponse writeAsPlainText(Object object)
            throws IOException {
        setContentType("text/plain");
        if (object instanceof String)
            getWriter().write((String) object);
        else if (null != object) {
            getWriter().write(object.toString());
        }
        return this;
    }

    public VimideHttpServletResponse writeAsJson(Object object)
            throws IOException {
        setContentType("application/json");
        getWriter().write(objectMapper.writeValueAsString(object));
        return this;
    }

    public VimideHttpServletResponse writeAsXml(Object object)
            throws IOException {
        return writeAsXDocument("text/xml", object);
    }

    public VimideHttpServletResponse writeAsHtml(Object object)
            throws IOException {
        return writeAsXDocument("text/html", object);
    }

    protected VimideHttpServletResponse writeAsXDocument(String contentType,
            Object object) throws IOException {
        setContentType(contentType);
        if (object instanceof String)
            getWriter().write((String) object);
        else if (null != object) {
            try {
                final JAXBContext context = JAXBContext.newInstance(object
                        .getClass());
                Marshaller marshaller = context.createMarshaller();
                marshaller.marshal(object, getWriter());
            } catch (JAXBException e) {
                LOGGER.error(
                        "Error caught at writing ({}) document response: {}",
                        new Object[] { contentType, e.getMessage() }, e);
            }
        }

        return this;
    }
}
