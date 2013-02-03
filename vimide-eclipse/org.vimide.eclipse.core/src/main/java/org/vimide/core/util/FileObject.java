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
package org.vimide.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

/**
 * File object to provide the interface of utilities.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class FileObject {

    private InputStream stream;
    private Integer[] offsets;
    private String[] multiByteLines;

    /**
     * Creates an new FileObject instance.
     * 
     * @param fis
     */
    public FileObject(FileInputStream fis) {
        this((InputStream) fis);
    }

    /**
     * Creates an new FileObject instance.
     * 
     * @param path
     * @throws FileNotFoundException
     */
    public FileObject(String path) throws FileNotFoundException {
        this(new File(path));
    }

    /**
     * Creates an new FileObject instance.
     * 
     * @param file
     * @throws FileNotFoundException
     */
    public FileObject(File file) throws FileNotFoundException {
        this(new FileInputStream(file));
    }

    /**
     * Creates an new FileObject instance.
     * 
     * @param is
     */
    public FileObject(InputStream is) {
        super();
        if (null == is)
            throw new IllegalArgumentException(
                    "Illegal input stream was supplied.");
        this.stream = is;
    }

    /**
     * Retrieves the content of the file object.
     * 
     * @return the content.
     */
    public String getContents() {
        try {
            return IOUtils.toString(stream);
        } catch (final IOException e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Parses the offsets list.
     * 
     * @throws IOException
     */
    protected void compileOffsets() throws IOException {
        List<Integer> lines = Lists.newArrayList(0);
        List<String> byteLines = Lists.newArrayList(StringUtils.EMPTY);
        String content = getContents();
        String lineDelimiter = "\n";

        int offset = 0;
        String line = null;
        String cacheLine = null;
        StringTokenizer tokenizer = new StringTokenizer(content, lineDelimiter,
                true);
        boolean newLine = false;
        while (tokenizer.hasMoreTokens()) {
            line = tokenizer.nextToken();

            if (null != cacheLine && !cacheLine.equals(lineDelimiter)
                    && line.equals(lineDelimiter)) {
                // the new line end with previous line.
                newLine = true;
            } else if (null != cacheLine && cacheLine.equals(lineDelimiter)
                    && line.equals(lineDelimiter)) {
                newLine = true;
            } else {
                newLine = false;
            }

            cacheLine = line;
            offset += line.length();

            if (newLine) {
                lines.add(new Integer(offset));
                if (line.length() != line.getBytes().length) {
                    byteLines.add(line);
                } else {
                    byteLines.add(null);
                }
            }
        }

        offsets = lines.toArray(new Integer[lines.size()]);
        multiByteLines = byteLines.toArray(new String[byteLines.size()]);
    }

    /**
     * Gets the line/column as int array by the supplied offset.
     * 
     * @param offset the specific offset
     * @return the line/column as int array
     */
    public int[] getLineColumn(int offset) {
        // performs the lazy compile.
        if (null == offsets) {
            try {
                compileOffsets();
            } catch (IOException e) {
            }
        }

        // return the minimize value if the offset was illegal.
        if (offset <= 0)
            return new int[] { 1, 1 };

        // r/b filtering the offset index in the compile offsets.
        int bot = -1;
        int top = offsets.length;

        while (top - bot > 1) {
            int mid = (top + bot) / 2;
            if (offsets[mid].intValue() < offset) {
                bot = mid;
            } else {
                top = mid;
            }
        }

        // resolves the line and column.
        int line = top;
        int column = 1 + offset - offsets[top - 1].intValue();
        String value = multiByteLines.length > line ? multiByteLines[line]
                : null;
        if (value != null) {
            column = value.substring(0, column).getBytes().length;
        }

        return new int[] { line, column };
    }

    /**
     * Gets the offset by the supplied line/column as int array.
     * 
     * @param linecol the int array of line/column.
     * @return the offset or -1 if the line/column was illegal.
     */
    public int getOffset(int[] linecol) {
        if (null != linecol && linecol.length == 2) {
            int line = linecol[0];
            int column = linecol[1];

            if (null == offsets) {
                try {
                    compileOffsets();
                } catch (IOException e) {
                }
            }

            if (line < 0 || line > offsets.length) {
                return -1;
            } else if (line == offsets.length) {
                if (multiByteLines[line - 1].getBytes().length > column)
                    return -1;
            }

            int offset = offsets[line - 1].intValue();
            offset += multiByteLines[line - 1].getBytes().length;

            return offset;
        }
        return -1;
    }

    /**
     * Finds the first line separator used by the given text.
     * 
     * @return </code>"\n"</code> or </code>"\r"</code> or </code>"\r\n"</code>,
     *         or <code>null</code> if none found
     */
    public static String findLineSeparator(char[] text) {
        // find the first line separator
        int length = text.length;
        if (length > 0) {
            char nextChar = text[0];
            for (int i = 0; i < length; i++) {
                char currentChar = nextChar;
                nextChar = i < length - 1 ? text[i + 1] : ' ';
                switch (currentChar) {
                    case '\n':
                        return "\n"; //$NON-NLS-1$
                    case '\r':
                        return nextChar == '\n' ? "\r\n" : "\r"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        // not found
        return null;
    }

    @Override
    public String toString() {
        String content = getContents();
        content = content.substring(0, 20) + "...";
        return content;
    }
}
