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
package org.vimide.eclipse.vimplugin.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.vimide.eclipse.vimplugin.VimideVimpluginPlugin;
import org.vimide.eclipse.vimplugin.VimpluginMessage;

/**
 * Vimplugin preference page. The fields are explained in
 * {@link PreferenceConstants}.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimpluginPreference extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public VimpluginPreference() {
        super(FieldEditorPreferencePage.GRID);
        setDescription(VimpluginMessage.preferences_description);
        setPreferenceStore(VimideVimpluginPlugin.getDefault()
                .getPreferenceStore());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        // do nothing here.
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        // if (!Os.isFamily(Os.FAMILY_MAC)) {
        // addField(new BooleanFieldEditor(
        // VimpluginPreferenceConstants.P_EMBED,
        // VimpluginMessage.preferences_embed, getFieldEditorParent()));
        // }
        //
        // addField(new
        // BooleanFieldEditor(VimpluginPreferenceConstants.P_TABBED,
        // VimpluginMessage.preferences_tabbed, getFieldEditorParent()));

        addField(new BooleanFieldEditor(
                VimpluginPreferenceConstants.P_FOCUS_AUTO_CLICK,
                VimpluginMessage.preferences_focus_click,
                getFieldEditorParent()));

        addField(new StringFieldEditor(VimpluginPreferenceConstants.P_PORT,
                VimpluginMessage.preferences_port, getFieldEditorParent()));

        addField(new StringFieldEditor(VimpluginPreferenceConstants.P_GVIM,
                VimpluginMessage.preferences_gvim, getFieldEditorParent()));

        addField(new StringFieldEditor(VimpluginPreferenceConstants.P_OPTS,
                VimpluginMessage.preferences_gvim_args, getFieldEditorParent()));
    }
}
