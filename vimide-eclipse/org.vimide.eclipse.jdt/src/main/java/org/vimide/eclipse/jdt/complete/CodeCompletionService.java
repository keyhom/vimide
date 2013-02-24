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
package org.vimide.eclipse.jdt.complete;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.vimide.eclipse.core.complete.CodeCompletionResult;
import org.vimide.eclipse.jdt.service.JavaBaseService;

import com.google.common.collect.Lists;

/**
 * The function service for code completion.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@SuppressWarnings("restriction")
public class CodeCompletionService extends JavaBaseService {

    private static final Comparator<CodeCompletionResult> COMPLETION_COMPARATOR = new CodeCompletionComparator();
    private static final String COMPACT = "compact";

    // private static final String STANDARD = "standard";

    /**
     * Singleton holder of {@link CodeCompletionService}.
     * 
     * @author keyhom (keyhom.c@gmail.com)
     */
    private static class SingletonHolder {
        static final CodeCompletionService instance = new CodeCompletionService();
    }

    /**
     * Gets the singleton instance.
     * 
     * @return singleton.
     */
    public static CodeCompletionService getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Creates an new CodeCompletionService instance in private.
     */
    private CodeCompletionService() {
        super();
    }

    /**
     * Calculates the code completion result.
     * 
     * @param src the source.
     * @param offset the char offset of the cursor.
     * @param layout the layout of result.
     * @return the code completion result.
     */
    public Object calculate(final ICompilationUnit src, final int offset,
            String layout) throws Exception {

        CodeCompletionProposalCollector collector = new CodeCompletionProposalCollector(
                src);
        src.codeComplete(offset, collector);

        IJavaCompletionProposal[] proposals = collector
                .getJavaCompletionProposals();

        List<CodeCompletionResult> results = Lists.newArrayList();

        for (int i = 0; i < proposals.length; i++) {
            results.add(createCompletionResult(collector, i, proposals[i]));
        }

        Collections.sort(results, COMPLETION_COMPARATOR);

        if (COMPACT.equals(layout) && !results.isEmpty()) {
            // results = compact(results);
        }

        return new CodeCompletionResponse(results, collector.getError(),
                collector.getImports()).toMap();
    }

    /**
     * Creates a code completion result from the supplied completion proposal.
     * 
     * @param collector the completion collector.
     * @param index the index of the proposal in the results.
     * @param proposal the proposal.
     * @return the result.
     */
    protected CodeCompletionResult createCompletionResult(
            CodeCompletionProposalCollector collector, int index,
            IJavaCompletionProposal proposal) {
        String completion = null;
        String menu = proposal.getDisplayString();

        if (proposal instanceof JavaCompletionProposal) {
            JavaCompletionProposal lazy = (JavaCompletionProposal) proposal;
            completion = lazy.getReplacementString();
            completion = completion.substring(lazy.getReplacementLength());
        } else if (proposal instanceof LazyJavaCompletionProposal) {
            LazyJavaCompletionProposal lazy = (LazyJavaCompletionProposal) proposal;
            completion = lazy.getReplacementString();  // lazy.getReplacementLength()
            completion = completion.substring(lazy.getReplacementLength());
        }

        int kind = collector.getProposal(index).getKind();
        switch (kind) {
            case CompletionProposal.METHOD_REF:
                // trim off the trailing parent if the method takes any
                // arguments.
                if (menu.lastIndexOf(')') > menu.lastIndexOf('(') + 1
                        && (completion.length() > 0 && completion
                                .charAt(completion.length() - 1) == ')')) {
                    completion = completion.substring(0,
                            completion.length() - 1);
                }
                break;
            case CompletionProposal.TYPE_REF:
                // trim off package info.
                int idx = completion.lastIndexOf('.');
                if (-1 != idx) {
                    completion = completion.substring(idx + 1);
                }
                break;
        }

        if ("class".equals(completion)) {
            kind = CompletionProposal.KEYWORD;
        }

        String type = "";
        switch (kind) {
            case CompletionProposal.TYPE_REF:
                type = CodeCompletionResult.TYPE;
                break;
            case CompletionProposal.FIELD_REF:
            case CompletionProposal.LOCAL_VARIABLE_REF:
                type = CodeCompletionResult.VARIABLE;
                break;
            case CompletionProposal.METHOD_REF:
                type = CodeCompletionResult.FUNCTION;
                break;
            case CompletionProposal.KEYWORD:
                type = CodeCompletionResult.KEYWORD;
                break;
        }

        // TODO:
        // hopefully Bram will take my advice to add lazy retrieval of
        // completion 'info' so that I can provide this text without the
        // overhead involved with retrieving it for every completion regardless
        // of whether the user ever views it.

        return new CodeCompletionResult(completion, menu, menu, type);
    }
}
