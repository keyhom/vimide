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
package org.vimide.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension2;

/**
 * Dummy implementation of IContentAssistantExtension2.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class DummyContentAssistantExtension2 implements IContentAssistant,
        IContentAssistantExtension2 {

    @Override
    public void addCompletionListener(ICompletionListener arg0) {
    }

    @Override
    public void removeCompletionListener(ICompletionListener arg0) {
    }

    @Override
    public void setEmptyMessage(String arg0) {
    }

    @Override
    public void setRepeatedInvocationMode(boolean arg0) {
    }

    @Override
    public void setShowEmptyList(boolean arg0) {
    }

    @Override
    public void setStatusLineVisible(boolean arg0) {
    }

    @Override
    public void setStatusMessage(String arg0) {
    }

    @Override
    public IContentAssistProcessor getContentAssistProcessor(String arg0) {
        return null;
    }

    @Override
    public void install(ITextViewer arg0) {
    }

    @Override
    public String showContextInformation() {
        return null;
    }

    @Override
    public String showPossibleCompletions() {
        return null;
    }

    @Override
    public void uninstall() {
    }

}

// vim:ft=java
