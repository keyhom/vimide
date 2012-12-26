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
package org.vimide.eclipse.vimplugin.editors;

import java.util.Arrays;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.contexts.IContextIds;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener responsible to disabling/enabling certain eclipse features as vim
 * editors gain and lose focus.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class VimEditorPartListener implements IPartListener {

    private static final Logger logger = LoggerFactory
            .getLogger(VimEditorPartListener.class);

    private static final String[] CONTEXT_IDS = new String[] {
            IContextIds.CONTEXT_ID_WINDOW,
            IContextIds.CONTEXT_ID_DIALOG_AND_WINDOW, };

    private boolean keysDisabled = false;
    private IBindingService bindingService;

    private String[] keys = { "Ctrl+PageUp", "Ctrl+PageDown", "Ctrl+M",
            "Ctrl+Shift+X" };

    private KeySequence[] keySequences;

    public VimEditorPartListener() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        bindingService = (IBindingService) workbench
                .getService(IBindingService.class);

        keySequences = new KeySequence[keys.length];
        for (int ii = 0; ii < keys.length; ii++) {
            try {
                keySequences[ii] = KeySequence.getInstance(keys[ii]);
            } catch (ParseException pe) {
                logger.error("Unable to parse keybinding: " + keys[ii], pe);
            }
        }

        // get context ids
        /*
         * IContextService contextService = (IContextService)
         * workbench.getService(IContextService.class); try{ for
         * (java.util.Iterator iterator = contextService.getDefinedContextIds()
         * .iterator(); iterator.hasNext();) { String id = (String)
         * iterator.next(); Context context = contextService.getContext(id);
         * String name = context.getName(); System.out.println("### id: " + id +
         * " name: " + name); } }catch(Exception e){ e.printStackTrace(); }
         */

        // get scheme ids
        /*
         * Scheme[] definedSchemes = bindingService.getDefinedSchemes(); try{
         * for (int i = 0; i < definedSchemes.length; i++) { Scheme scheme =
         * definedSchemes[i]; String name = scheme.getName(); String id =
         * scheme.getId(); System.out.println("### id: " + id + " name: " +
         * name); } }catch(Exception e){ e.printStackTrace(); // Do nothing. }
         */
    }

    /**
     * {@inheritDoc}
     * 
     * @see IPartListener#partActivated(IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part) {
        if (part instanceof VimEditor) {
            VimEditor editor = (VimEditor) part;
            if (editor.isEmbedded()) {
                disableKeys();
            }
        } else {
            enableKeys();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see IPartListener#partBroughtToTop(IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part) {
    }

    /**
     * {@inheritDoc}
     * 
     * @see IPartListener#partClosed(IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part) {
    }

    /**
     * {@inheritDoc}
     * 
     * @see IPartListener#partDeactivated(IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part) {
    }

    /**
     * {@inheritDoc}
     * 
     * @see IPartListener#partOpened(IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part) {
    }

    private BindingManager getLocalChangeManager() {
        BindingManager manager = new BindingManager(new ContextManager(),
                new CommandManager());

        Scheme scheme = bindingService.getActiveScheme();
        try {
            try {
                manager.setActiveScheme(scheme);
            } catch (NotDefinedException ignore) {
            }

            manager.setLocale(bindingService.getLocale());
            manager.setPlatform(bindingService.getPlatform());
            manager.setBindings(bindingService.getBindings());
        } catch (Exception e) {
            logger.error("Error initializing local binding manager.", e);
        }

        return manager;
    }

    private void disableKeys() {
        if (!keysDisabled) {
            logger.debug("Disabling conflicting keybindings while vim editor is focused: "
                    + Arrays.toString(keys));
            BindingManager localChangeManager = getLocalChangeManager();
            String schemeId = localChangeManager.getActiveScheme().getId();
            for (KeySequence keySequence : keySequences) {
                for (String contextId : CONTEXT_IDS) {
                    localChangeManager.removeBindings(keySequence, schemeId,
                            contextId, null, null, null, Binding.USER);
                    localChangeManager.addBinding(new KeyBinding(keySequence,
                            null, schemeId, contextId, null, null, null,
                            Binding.USER));
                }
            }
            keysDisabled = true;
            saveKeyChanges(localChangeManager);
        }
    }

    private void enableKeys() {
        if (keysDisabled) {
            logger.debug("Re-enabling conflicting keybindings.");
            BindingManager localChangeManager = getLocalChangeManager();
            String schemeId = localChangeManager.getActiveScheme().getId();
            for (KeySequence keySequence : keySequences) {
                for (String contextId : CONTEXT_IDS) {
                    localChangeManager.removeBindings(keySequence, schemeId,
                            contextId, null, null, null, Binding.USER);
                }
            }
            keysDisabled = false;
            saveKeyChanges(localChangeManager);
        }
    }

    private void saveKeyChanges(BindingManager localChangeManager) {
        try {
            bindingService.savePreferences(
                    localChangeManager.getActiveScheme(),
                    localChangeManager.getBindings());
        } catch (Exception e) {
            logger.error("Error persisting key bindings.", e);
        }
    }
}
