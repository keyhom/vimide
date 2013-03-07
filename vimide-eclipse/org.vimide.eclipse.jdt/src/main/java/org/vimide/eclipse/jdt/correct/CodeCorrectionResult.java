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
package org.vimide.eclipse.jdt.correct;

/**
 * Holds information about a correction proposal.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class CodeCorrectionResult {

    private int index;
    private String description;
    private String preview;

    /**
     * Default constructor.
     * 
     * @param index the index of this result in relation to other proposals.
     * @param description the description of the proposed correction.
     * @param preview a preview of the code after applying the correction.
     */
    public CodeCorrectionResult(int index, String description, String preview) {
        this.index = index;
        this.description = description;
        this.preview = preview;
    }

    /**
     * Gets the index of this result.
     * 
     * @return the index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets a description of the proposed correction.
     * 
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets a code snip-it preview of the result of applying the proposed
     * correction.
     * 
     * @return a preview.
     */
    public String getPreview() {
        return preview;
    }
}
