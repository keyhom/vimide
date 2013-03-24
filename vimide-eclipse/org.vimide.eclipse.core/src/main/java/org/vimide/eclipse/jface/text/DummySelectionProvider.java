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
package org.vimide.eclipse.jface.text;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * Dummy ISelectionProvider implementation that provides basic functionality for
 * eclipse classes that require an ITextViewer.
 *
 * @author keyhom (keyhom.c@gmail.com)
 */
public class DummySelectionProvider implements ISelectionProvider {

    private ISelection selection;

    /**
     * Default constructor by the ISelection.
     *
     * @param selection the ISelection instance.
     */
    public DummySelectionProvider(ISelection selection) {
        this.selection = selection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener arg0) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISelection getSelection() {
        return selection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener arg0) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelection(ISelection selection) {
        this.selection = selection;
    }

}

// vim:ft=java
