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

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * An implementation of HttpServletRequest for Vimide.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimideHttpServletRequest extends HttpServletRequestWrapper {

    private static final String EMPTY_STRING = "";
    static final Logger LOGGER = LoggerFactory
            .getLogger(VimideHttpServletRequest.class);

    /**
     * Creates an new VimideHttpServletRequest instance.
     * 
     * @param request
     */
    public VimideHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    public byte getByteParameter(String name) {
        return getByteParameter(name, (byte) 0);
    }

    public byte getByteParameter(String name, byte defaultValue) {
        final String value = getParameter(name);
        if (null != value && !value.isEmpty()) {
            try {
                return Byte.valueOf(value);
            } catch (final NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public byte[] getByteParameterValues(String name) {
        return getByteParameterValues(name, (byte) 0);
    }

    public byte[] getByteParameterValues(String name, byte defaultValue) {
        final String[] pValues = getParameterValues(name);
        if (null != pValues) {
            final byte[] values = new byte[pValues.length];

            for (int i = 0; i < pValues.length; i++) {
                String pValue = pValues[i];
                if (!Strings.isNullOrEmpty(pValue)) {
                    values[i] = Byte.valueOf(pValue.trim());
                } else {
                    values[i] = defaultValue;
                }
            }
            return values;

        }
        return null;
    }

    public float getFloatParameter(String name) {
        return getFloatParameter(name, .0f);
    }

    public float getFloatParameter(String name, float defaultValue) {
        final String value = getParameter(name);
        if (null != value && !value.isEmpty()) {
            try {
                return Float.valueOf(value);
            } catch (final NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public float[] getFloatParameterValues(String name) {
        return getFloatParameterValues(name, 0);
    }

    public float[] getFloatParameterValues(String name, float defaultValue) {
        final String[] pValues = getParameterValues(name);
        if (null != pValues) {
            final float[] values = new float[pValues.length];

            for (int i = 0; i < pValues.length; i++) {
                String pValue = pValues[i];
                if (!Strings.isNullOrEmpty(pValue)) {
                    values[i] = Float.valueOf(pValue.trim());
                } else {
                    values[i] = defaultValue;
                }
            }
            return values;
        }
        return null;
    }

    public double getDoubleParameter(String name) {
        return getDoubleParameter(name, .0f);
    }

    public double getDoubleParameter(String name, double defaultValue) {
        final String value = getParameter(name);
        if (null != value && !value.isEmpty()) {
            try {
                return Double.valueOf(value);
            } catch (final NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public double[] getDoubleParameterValues(String name) {
        return getDoubleParameterValues(name, .0f);
    }

    public double[] getDoubleParameterValues(String name, double defaultValue) {
        final String[] pValues = getParameterValues(name);
        if (null != pValues) {
            final double[] values = new double[pValues.length];

            for (int i = 0; i < pValues.length; i++) {
                String pValue = pValues[i];
                if (!Strings.isNullOrEmpty(pValue)) {
                    values[i] = Double.valueOf(pValue.trim());
                } else {
                    values[i] = defaultValue;
                }
            }
            return values;
        }
        return null;
    }

    /**
     * Gets the value as integer by the specified name, return <code>zero</code>
     * if the value is an illegal number.
     * 
     * @param name the name of parameter.
     * @return value as integer.
     */
    public int getIntParameter(String name) {
        return getIntParameter(name, 0);
    }

    /**
     * Gets the value as integer by the specified name, return
     * <code>defaultValue</code> if the value is an illegal number or
     * non-existing.
     * 
     * @param name the name of parameter.
     * @param defaultValue the value of default injection.
     * @return value as integer.
     */
    public int getIntParameter(String name, int defaultValue) {
        final String value = getParameter(name);
        if (null != value && !value.isEmpty()) {
            try {
                return Integer.valueOf(value);
            } catch (final NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public int[] getIntParameterValues(String name) {
        return getIntParameterValues(name, 0);
    }

    public int[] getIntParameterValues(String name, int defaultValue) {
        final String[] pValues = getParameterValues(name);

        if (null != pValues) {
            final int[] values = new int[pValues.length];

            for (int i = 0; i < pValues.length; i++) {
                String pValue = pValues[i];
                if (!Strings.isNullOrEmpty(pValue)) {
                    values[i] = Integer.valueOf(pValue.trim());
                } else {
                    values[i] = defaultValue;
                }
            }
            return values;
        }
        return null;
    }

    public long getLongParameter(String name) {
        return getLongParameter(name, 0);
    }

    public long getLongParameter(String name, long defaultValue) {
        final String value = getParameter(name);
        if (null != value && !value.isEmpty()) {
            try {
                return Long.valueOf(value);
            } catch (final NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public long[] getLongParameterValues(String name) {
        return getLongParameterValues(name, 0);
    }

    public long[] getLongParameterValues(String name, long defaultValue) {
        final String[] pValues = getParameterValues(name);
        if (null != pValues) {
            final long[] values = new long[pValues.length];

            for (int i = 0; i < pValues.length; i++) {
                String pValue = pValues[i];
                if (!Strings.isNullOrEmpty(pValue)) {
                    values[i] = Long.valueOf(pValue.trim());
                } else {
                    values[i] = defaultValue;
                }
            }
            return values;
        }
        return null;
    }

    /**
     * Gets the value by the specified name, if the value was null, then return
     * the empty string instead of.
     * 
     * @param name the name of parameter.
     * @return value never be null.
     */
    public String getNotNullParameter(String name) {
        return getNotNullParameter(name, EMPTY_STRING);
    }

    /**
     * Gets the value by the specified name, if the value was null, then return
     * the <code>defaultValue</code> instead of.
     * 
     * @param name the name of parameter.
     * @param defaultValue the default value of parameter.
     * @return value or default value.
     */
    public String getNotNullParameter(String name, String defaultValue) {
        final String value = getParameter(name);
        if (null == value || value.isEmpty())
            return defaultValue;
        return value;
    }

    public String[] getNotNullParameterValues(String name) {
        return getNotNullParameterValues(name, EMPTY_STRING);
    }

    public String[] getNotNullParameterValues(String name, String defaultValue) {
        final String[] pValues = getParameterValues(name);

        if (null != pValues)
            for (int i = 0; i < pValues.length; i++) {
                String pValue = pValues[i];
                if (!Strings.isNullOrEmpty(pValue)) {
                    pValues[i] = pValue.trim();
                } else {
                    pValues[i] = defaultValue;
                }
            }

        return pValues;
    }

    public Date getDateParameter(String name) {
        return getDateParameter(name, new Date(0));
    }

    public Date getDateParameter(String name, Date defaultValue) {
        String value = getNotNullParameter(name);

        if (value.isEmpty())
            return defaultValue;

        try {
            return DateFormat.getDateInstance().parse(value);
        } catch (ParseException e) {
            LOGGER.warn("Wrong date format was submit: {}", e.getMessage());
            return defaultValue;
        }
    }

    public Timestamp getTimestampParameter(String name) {
        long value = getLongParameter(name);
        return new Timestamp(value);
    }

    public File getFileParameter(String name) {
        String fileValue = getNotNullParameter(name);
        if (!fileValue.isEmpty()) {
            return new File(fileValue);
        }
        return null;
    }
}
