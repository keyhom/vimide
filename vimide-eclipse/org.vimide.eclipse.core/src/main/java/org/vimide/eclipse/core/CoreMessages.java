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
package org.vimide.eclipse.core;

import org.eclipse.osgi.util.NLS;

/**
 * Core messages bindings.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class CoreMessages extends NLS {

    private static final String BUNDLE_NAME = "org.vimide.eclipse.core.messages";
    
    /* Misc messages */
    public static String welcome_message;
    public static String history_created;
    public static String vim_script_updated;

    /* Plugins messages. */
    public static String plugin_load_failed;
    public static String plugin_reloaded;
    
    /* Project status messages. */
    public static String project_created;
    public static String project_imported;
    public static String project_updated;
    public static String project_deleted;
    public static String project_renamed;
    public static String project_moved;
    public static String project_refreshed;
    public static String project_built;
    public static String project_built_all;
    public static String project_opened;
    public static String project_closed;
    public static String project_nature_added;
    public static String project_nature_removed;
    
    /* Project messages.*/
    public static String project_not_exists;
    public static String project_name_exists;
    public static String project_not_found;
    public static String project_missing_nature;
    public static String project_depends_not_found;
    public static String project_loaction_null;
    public static String project_copyright_not_found;
    public static String project_file_mismatch;
    public static String project_import_failed;
    public static String project_directory_missing;
    public static String project_segments_error;
    public static String project_dotproject_missing;
    public static String project_op_failed;
    public static String project_limit_closed_for;
    
    /* Settings messages.*/
    public static String setting_not_found;
    public static String setting_invalid;
    public static String setting_invalid_regex;
    
    /* Settings description messages.*/
    public static String ls_keyhom_vimide_user_name;
    public static String ls_keyhom_vimide_user_email;
    public static String ls_keyhom_vimide_project_version;
    public static String ls_keyhom_vimide_project_copyright;

    /* Files messages. */
    public static String file_not_found;
    public static String file_updated;
    public static String dir_not_found;
    public static String src_file_not_found;
    public static String template_not_found;
    public static String template_eval_error;
    public static String template_type_invalid;

    public static String script_not_found;
    public static String resource_unable_resolve;
    public static String required_options_missing;
    public static String invalid_options;

    /* tip messages */
    public static String no_element_not_found;
    public static String no_element_not_found_at;

    static {
        NLS.initializeMessages(BUNDLE_NAME, CoreMessages.class);
    }
}
