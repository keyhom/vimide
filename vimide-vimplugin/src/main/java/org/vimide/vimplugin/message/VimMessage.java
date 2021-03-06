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
package org.vimide.vimplugin.message;

import java.io.Serializable;

/**
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public interface VimMessage extends Serializable {

    /**
     * Gets the value of data property.
     * 
     * @return the data
     */
    String getData();

    /**
     * Gets the value of seqno property.
     * 
     * @return the seqno
     */
    int getSeqno();

    /**
     * Gets the value of name property.
     * 
     * @return the name
     */
    String getName();

    /**
     * Gets the value of bufferId property.
     * 
     * @return the bufferId
     */
    int getBufferId();

    int[] nextLnumCol() throws Exception;

    boolean nextBoolean() throws Exception;

    int nextNumber() throws Exception;

    String nextString() throws Exception;

}
