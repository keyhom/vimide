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
package org.vimide.eclipse.core.complete;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.common.base.Strings;

/**
 * Represents a code completion result.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class CodeCompletionResult {

    private static final Pattern FIRST_LINE = Pattern
            .compile("(\\.\\s|\\.<|<br|<BR|<p|<P)");

    private static final int MAX_SHORT_DESCRIPTION_LENGTH = 74;

    public static final String VARIABLE = "v";
    public static final String FUNCTION = "f";
    public static final String TYPE = "t";
    public static final String KEYWORD = "k";
    public static final String METHOD = "m";

    /**
     * Creates the menu text based on the supplied text info.
     * 
     * @param info the info text.
     * @return the menu text.
     */
    public static String menuFromInfo(String info) {
        if (null == info)
            return null;

        String menu = info;
        Matcher matcher = FIRST_LINE.matcher(menu);
        if (menu.length() > 1 && matcher.find(1)) {
            menu = menu.substring(0, matcher.start() + 1);
            if (menu.endsWith("<")) {
                menu = menu.substring(0, menu.length() - 1);
            }
        }
        menu = menu.replaceAll("\n", StringUtils.EMPTY);
        menu = menu.replaceAll("<.*?>", StringUtils.EMPTY);

        return StringUtils.abbreviate(menu, MAX_SHORT_DESCRIPTION_LENGTH);
    }

    private String completion;
    private String menu;
    private String info;
    private String type;

    /**
     * Creates an new CodeCompletionResult instance.
     * 
     * @param completion
     * @param menu
     * @param info
     * @param type
     */
    public CodeCompletionResult(String completion, String menu, String info,
            String type) {
        this.completion = completion;
        this.menu = menu;
        this.info = info;
        this.type = null != type ? type : StringUtils.EMPTY;

        if (!Strings.isNullOrEmpty(this.info)) {
            this.info = StringUtils.replace(this.info, "\n", "<br/>");
        }

        if (!Strings.isNullOrEmpty(this.menu)) {
            this.menu = StringUtils.replace(this.menu, "\n", "<br/>");
        }

        if (!Strings.isNullOrEmpty(this.info)
                && !Strings.isNullOrEmpty(this.menu)) {
            this.menu = menuFromInfo(this.info);
        }
    }

    /**
     * Gets the completion.
     * 
     * @return completion.
     */
    public String getCompletion() {
        return completion;
    }

    /**
     * Gets the completion info.
     * 
     * @return completion info.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Gets the menu text.
     * 
     * @return the menu text.
     */
    public String getMenu() {
        return menu;
    }

    /**
     * Gets the type of this completion.
     * 
     * @return the type.
     */
    public String getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CodeCompletionResult))
            return false;
        if (this == other)
            return true;

        CodeCompletionResult result = (CodeCompletionResult) other;
        return new EqualsBuilder()
                .append(getCompletion(), result.getCompletion())
                .append(getMenu(), result.getMenu())
                .append(getType(), result.getType()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(18, 38).append(completion).append(menu)
                .toHashCode();
    }
}