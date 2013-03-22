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
package org.vimide.eclipse.jface.text;

import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

/**
 * Dummy ITextViewer implementation that provides basic functionality for
 * eclipse classes that requires an ITextViewer.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class DummyTextViewer implements ITextViewer {

    IDocument document;
    ISelectionProvider selectionProvider;

    /**
     * Default constructor.
     * 
     * @param document the document.
     * @param offset the offset.
     * @param length the length.
     */
    public DummyTextViewer(IDocument document, int offset, int length) {
        super();

        this.document = document;
        // selectionProvider = new DummySelectionProvider(new
        // TextSelection(document, offset, length));
    }

    @Override
    public void activatePlugins() {
    }

    @Override
    public void addTextInputListener(ITextInputListener arg0) {
    }

    @Override
    public void addTextListener(ITextListener arg0) {
    }

    @Override
    public void addViewportListener(IViewportListener arg0) {
    }

    @Override
    public void changeTextPresentation(TextPresentation arg0, boolean arg1) {
    }

    @Override
    public int getBottomIndex() {
        return -1;
    }

    @Override
    public int getBottomIndexEndOffset() {
        return -1;
    }

    @Override
    public IDocument getDocument() {
        return document;
    }

    @Override
    public IFindReplaceTarget getFindReplaceTarget() {
        return null;
    }

    @Override
    public Point getSelectedRange() {
        return new Point(-1, -1);
    }

    @Override
    public ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    @Override
    public ITextOperationTarget getTextOperationTarget() {
        return null;
    }

    @Override
    public StyledText getTextWidget() {
        return null;
    }

    @Override
    public int getTopIndex() {
        return -1;
    }

    @Override
    public int getTopIndexStartOffset() {
        return -1;
    }

    @Override
    public int getTopInset() {
        return -1;
    }

    @Override
    public IRegion getVisibleRegion() {
        return null;
    }

    @Override
    public void invalidateTextPresentation() {
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public boolean overlapsWithVisibleRegion(int arg0, int arg1) {
        return false;
    }

    @Override
    public void removeTextInputListener(ITextInputListener arg0) {
    }

    @Override
    public void removeTextListener(ITextListener arg0) {
    }

    @Override
    public void removeViewportListener(IViewportListener arg0) {
    }

    @Override
    public void resetPlugins() {
    }

    @Override
    public void resetVisibleRegion() {
    }

    @Override
    public void revealRange(int arg0, int arg1) {
    }

    @Override
    public void setAutoIndentStrategy(IAutoIndentStrategy arg0, String arg1) {
    }

    @Override
    public void setDefaultPrefixes(String[] arg0, String arg1) {
    }

    @Override
    public void setDocument(IDocument arg0) {
    }

    @Override
    public void setDocument(IDocument arg0, int arg1, int arg2) {
    }

    @Override
    public void setEditable(boolean arg0) {
    }

    @Override
    public void setEventConsumer(IEventConsumer arg0) {
    }

    @Override
    public void setIndentPrefixes(String[] arg0, String arg1) {
    }

    @Override
    public void setSelectedRange(int arg0, int arg1) {
    }

    @Override
    public void setTextColor(Color arg0) {
    }

    @Override
    public void setTextColor(Color arg0, int arg1, int arg2, boolean arg3) {
    }

    @Override
    public void setTextDoubleClickStrategy(ITextDoubleClickStrategy arg0,
            String arg1) {
    }

    @Override
    public void setTextHover(ITextHover arg0, String arg1) {
    }

    @Override
    public void setTopIndex(int arg0) {
    }

    @Override
    public void setUndoManager(IUndoManager arg0) {
    }

    @Override
    public void setVisibleRegion(int arg0, int arg1) {
    }

}

// vim:ft=java
