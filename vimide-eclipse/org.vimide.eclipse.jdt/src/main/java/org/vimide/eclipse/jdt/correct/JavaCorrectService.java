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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.internal.ui.text.correction.ReorgCorrectionsSubProcessor;
import org.eclipse.jdt.internal.ui.text.correction.proposals.NewCUUsingWizardProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEdit;
import org.vimide.eclipse.core.refactoring.ResourceChangeListener;
import org.vimide.eclipse.jdt.JavaSourceFacade;
import org.vimide.eclipse.jdt.JdtMessages;
import org.vimide.eclipse.jdt.service.JavaBaseService;
import org.vimide.eclipse.jdt.service.JavaSourceService;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Handles code correction.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@SuppressWarnings("restriction")
@WebServlet(urlPatterns = "/javaCorrect")
public class JavaCorrectService extends JavaBaseService {

    /**
     * Singleton holder for {@link JavaCorrectService}.
     */
    private static class SingletonHolder {
        static final JavaCorrectService instance = new JavaCorrectService();
    }

    private final static Set<Class<? extends IJavaCompletionProposal>> IGNORE_BY_TYPE = Sets
            .newHashSet();
    static {
        IGNORE_BY_TYPE.add(NewCUUsingWizardProposal.class);
        IGNORE_BY_TYPE
                .add(ReorgCorrectionsSubProcessor.ClasspathFixCorrectionProposal.class);
    }

    private static final Set<String> IGNORE_BY_INFO = Sets.newHashSet();
    static {
        IGNORE_BY_INFO
                .add(CorrectionMessages.LocalCorrectionsSubProcessor_InferGenericTypeArguments_description);
    }

    private static Class<?> ASTRewriteCorrectionProposal = null;
    private static Class<?> ChangeCorrectionProposal = null;

    static {
        final ClassLoader loader = JavaCorrectService.class.getClassLoader();
        try {
            ASTRewriteCorrectionProposal = loader
                    .loadClass("org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal");
        } catch (final Exception ignore) {
        }

        try {
            if (null == ASTRewriteCorrectionProposal)
                ASTRewriteCorrectionProposal = loader
                        .loadClass("org.eclipse.jdt.internal.ui.text.correction.proposals.ASTRewriteCorrectionProposal");
        } catch (final Exception ignore) {
        }

        try {
            ChangeCorrectionProposal = loader
                    .loadClass("org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal");
        } catch (final Exception ignore) {
        }

        try {
            if (null == ChangeCorrectionProposal)
                ChangeCorrectionProposal = loader
                        .loadClass("org.eclipse.jdt.internal.ui.text.correction.proposals.ChangeCorrectionProposal");
        } catch (final Exception ignore) {
        }
    }

    /**
     * Gets the singleton instance.
     * 
     * @return singleton.
     */
    public static JavaCorrectService getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Creates the JavaCorrectService instance by private.
     */
    private JavaCorrectService() {
        super();
    }

    /**
     * Makes the correct suggestion or apply the correct suggestion by the
     * supplied position info.
     * 
     * @param src the source file.
     * @param line the line the problem located at.
     * @param offset the offset the problem located at.
     * @param apply the index of proposal or < 0 for no apply.
     * @return the result of make correction.
     * @throws Exception
     */
    public Object makeCorrect(ICompilationUnit src, int line, int offset,
            int apply) throws Exception {
        IProblem problem = getProblem(src, line, offset);
        if (null == problem) {
            String message = NLS
                    .bind(JdtMessages.no_element_not_found_at, new Object[] {
                            "error", src.getPath(), String.valueOf(line) });
            return message;
        }

        List<IJavaCompletionProposal> proposals = getProposals(src, problem);
        if (apply >= 0) {
            IJavaCompletionProposal proposal = proposals.get(apply);
            return apply(src, proposal);
        }

        final Map<String, Object> result = Maps.newHashMap();
        result.put("message", problem.getMessage());
        result.put("offset", problem.getSourceStart());
        result.put("corrections", getCorrections(proposals));

        return result;
    }

    /**
     * Gets possible corrections for the supplied problem.
     * 
     * @param src the source file.
     * @param problem the problem.
     * @return a list of ChangeCorrectionProposal.
     * @throws Exception
     */
    protected List<IJavaCompletionProposal> getProposals(ICompilationUnit src,
            IProblem problem) throws Exception {
        IProject project = src.getJavaProject().getProject();
        List<IJavaCompletionProposal> results = Lists.newArrayList();

        int length = (problem.getSourceEnd() + 1) - problem.getSourceStart();
        AssistContext context = new AssistContext(src,
                problem.getSourceStart(), length);

        IProblemLocation[] locations = new IProblemLocation[] { new ProblemLocation(
                problem) };
        IQuickFixProcessor[] processors = getQuickFixProcessors(src);

        Method getImportRewrite = ASTRewriteCorrectionProposal
                .getMethod("getImportRewrite");
        for (IQuickFixProcessor p : processors) {
            if (null != p && p.hasCorrections(src, problem.getID())) {
                // we currently don't support the ajdt processor since it relies
                // on PlatformUI.getWorkbench().getActiveWorkbenchWindow() which
                // is null here.
                if (p.getClass()
                        .getName()
                        .equals("org.eclipse.ajdt.internal.ui.editor.quickfix.QuickFixProcessor")) {
                    continue;
                }

                IJavaCompletionProposal[] proposals = p.getCorrections(context,
                        locations);
                if (null != proposals) {
                    for (IJavaCompletionProposal proposal : proposals) {
                        if (!ChangeCorrectionProposal.isInstance(proposal)) {
                            continue;
                        }

                        // skip proposal requiring gui dialogs, etc.
                        if (IGNORE_BY_TYPE.contains(proposal.getClass())
                                || IGNORE_BY_INFO.contains(proposal
                                        .getAdditionalProposalInfo())) {
                            continue;
                        }

                        // honer the user's import exclusions.
                        if (ASTRewriteCorrectionProposal.isInstance(proposal)) {
                            ImportRewrite rewrite = (ImportRewrite) getImportRewrite
                                    .invoke(proposal);
                            if (null != rewrite
                                    && (rewrite.getAddedImports().length != 0 || rewrite
                                            .getAddedStaticImports().length != 0)) {
                                boolean exclude = true;
                                JavaSourceService sourceService = JavaSourceService
                                        .getInstance();
                                for (String fqn : rewrite.getAddedImports()) {
                                    if (!sourceService.isImportExcluded(
                                            project, fqn)) {
                                        exclude = false;
                                        break;
                                    }
                                }

                                for (String fqn : rewrite
                                        .getAddedStaticImports()) {
                                    if (!sourceService.isImportExcluded(
                                            project, fqn)) {
                                        exclude = false;
                                        break;
                                    }
                                }

                                if (exclude) {
                                    continue;
                                }
                            }
                        }

                        results.add(proposal);
                    }
                }
            }
        }

        Collections.sort(results, new CompletionProposalComparator());
        return results;
    }

    /**
     * Converts the supplied list of IJavaCompletionProposal(s) to array of
     * CodeCorrectionResult.
     * 
     * @param proposals list of IJavaCompletionProposal.
     * @return array of CodeCorrectionResult.
     * @throws Exception
     */
    protected List<CodeCorrectionResult> getCorrections(
            List<IJavaCompletionProposal> proposals) throws Exception {
        List<CodeCorrectionResult> corrections = Lists.newArrayList();
        int index = 0;
        for (IJavaCompletionProposal proposal : proposals) {
            String preview = proposal.getAdditionalProposalInfo();
            if (null != preview) {
                preview = preview.replaceAll("<br>", "\n")
                        .replaceAll("<br/>", "\n").replaceAll("<.+?>", "")
                        .replaceAll("&lt;", "<").replaceAll("&gt;", ">");
            }
            corrections.add(new CodeCorrectionResult(index, proposal
                    .getDisplayString(), preview));
            index++;
        }
        return corrections;
    }

    /**
     * Apply the supplied correction proposal.
     * 
     * @param src the source file.
     * @param proposal the proposal to apply.
     * @return a list of changed files or a map containing a list of errors.
     * @throws Exception
     */
    protected Object apply(ICompilationUnit src,
            IJavaCompletionProposal proposal) throws Exception {
        Change change = null;
        try {
            NullProgressMonitor monitor = new NullProgressMonitor();
            change = (Change) ChangeCorrectionProposal.getMethod("getChange")
                    .invoke(proposal);
            change.initializeValidationData(monitor);
            RefactoringStatus status = change.isValid(monitor);
            if (status.hasFatalError()) {
                List<String> errors = Lists.newArrayList();
                for (RefactoringStatusEntry entry : status.getEntries()) {
                    String message = entry.getMessage();
                    if (!errors.contains(message)
                            && !message.startsWith("Found potential matches")) {
                        errors.add(message);
                    }
                }

                Map<String, List<String>> result = Maps.newHashMap();
                result.put("errors", errors);
                return result;
            }

            ResourceChangeListener rcl = new ResourceChangeListener();
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            workspace.addResourceChangeListener(rcl);

            try {
                TextEdit edit = null;
                if (change instanceof TextFileChange) {
                    TextFileChange fileChange = (TextFileChange) change;
                    fileChange.setSaveMode(TextFileChange.FORCE_SAVE);
                    edit = fileChange.getEdit();
                }

                PerformChangeOperation changeOperation = new PerformChangeOperation(
                        change);
                String name = (String) ChangeCorrectionProposal.getMethod(
                        "getName").invoke(proposal);
                changeOperation.setUndoManager(
                        RefactoringCore.getUndoManager(), name);
                changeOperation.run(monitor);

                if (null != edit
                        && change instanceof CompilationUnitChange
                        && src.equals(((CompilationUnitChange) change)
                                .getCompilationUnit())) {
                    JavaSourceFacade.format(src, edit.getOffset(),
                            edit.getLength());
                }

                return rcl.getChangedFiles();
            } finally {
                if (null != workspace)
                    workspace.removeResourceChangeListener(rcl);
            }
        } finally {
            if (null != change)
                change.dispose();
        }
    }
}
