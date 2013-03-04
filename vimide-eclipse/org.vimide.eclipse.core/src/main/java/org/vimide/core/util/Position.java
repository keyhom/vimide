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
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Represents a position within a file as denoted by an offset and length.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class Position implements Serializable {

    private static final long serialVersionUID = 1L;

    public static Position fromOffset(String fileName, String message,
            int offset, int length) {
        int line = 1;
        int column = 1;

        try {
            File file = new File(fileName);
            URL fileUrl = null;
            InputStream stream = null;
            
            if (!file.exists()) {
                if (0 < file.getAbsolutePath().indexOf('!')) {
                    // URL ?
                    try {
                        fileUrl = new URL(fileName);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            if (null != fileUrl) {
                try {
                    stream = fileUrl.openStream();
                } catch (IOException e) {
                }
            }
            
            if (null == stream) {
                stream = new FileInputStream(file);
            }
            
            int[] pos = new FileObject(stream)
                    .getLineColumn(offset);
            if (null != pos) {
                line = pos[0];
                column = pos[1];
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return new Position(fileName, message, offset, length, line, column);
    }

    public static Position fromLineColumn(String fileName, String message,
            int line, int column) {
        return new Position(fileName, message, 0, 0, line, column);
    }

    private String fileName;
    private int offset = 0;
    private int length = 0;
    private int line = 1;
    private int column = 1;
    private String message;

    private Position(String filename, String message, int offset, int length,
            int line, int column) {
        this.fileName = filename;
        this.message = null != message ? message : StringUtils.EMPTY;
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    /**
     * Gets the value of serialversionuid property.
     * 
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * Gets the value of fileName property.
     * 
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the value of offset property.
     * 
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets the value of length property.
     * 
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the value of line property.
     * 
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the value of column property.
     * 
     * @return the column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Gets the value of message property.
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) {
            return false;
        }

        if (this == obj)
            return true;
        Position rhs = (Position) obj;
        return new EqualsBuilder().append(fileName, rhs.fileName)
                .append(offset, rhs.offset).append(length, rhs.length)
                .append(line, rhs.line).append(column, rhs.column).isEquals();
    }

}