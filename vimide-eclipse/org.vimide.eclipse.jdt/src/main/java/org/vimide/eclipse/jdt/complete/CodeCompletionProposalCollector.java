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

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.vimide.core.util.FileObject;
import org.vimide.eclipse.core.util.EclipseResourceUtil;
import org.vimide.eclipse.jdt.search.SearchRequestor;
import org.vimide.eclipse.jdt.service.JavaSourceService;

import com.google.common.collect.Lists;

/**
 * Extension to eclipse CompletionProposalCollector that saves reference to
 * original CompletionProposals.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@SuppressWarnings("restriction")
public class CodeCompletionProposalCollector extends
        CompletionProposalCollector {

    private List<CompletionProposal> proposals = Lists.newArrayList();
    private List<String> imports;
    private Map<String, Object> error;

    /**
     * Creates an new CodeCompletionProposalCollector instance.
     * 
     * @param cu
     */
    public CodeCompletionProposalCollector(ICompilationUnit cu) {
        super(cu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(CompletionProposal proposal) {
        try {
            if (isFiltered(proposal)) {
                return;
            }

            if (CompletionProposal.POTENTIAL_METHOD_DECLARATION != proposal
                    .getKind()) {
                switch (proposal.getKind()) {
                    case CompletionProposal.KEYWORD:
                    case CompletionProposal.PACKAGE_REF:
                    case CompletionProposal.TYPE_REF:
                    case CompletionProposal.FIELD_REF:
                    case CompletionProposal.METHOD_REF:
                    case CompletionProposal.METHOD_NAME_REFERENCE:
                    case CompletionProposal.METHOD_DECLARATION:
                    case CompletionProposal.ANONYMOUS_CLASS_DECLARATION:
                    case CompletionProposal.LABEL_REF:
                    case CompletionProposal.LOCAL_VARIABLE_REF:
                    case CompletionProposal.VARIABLE_DECLARATION:
                    case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
                        proposals.add(proposal);
                        super.accept(proposal);
                        break;
                    default:
                        // do nothing.
                }
            }
        } catch (final IllegalArgumentException e) {
            // all signature processing method may throw IAEs
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84657
            // don't abort, but log and show all the valid proposals.
            JavaPlugin.log(new Status(IStatus.ERROR, JavaPlugin.getPluginId(),
                    IStatus.OK, "Exception when processing proposal for: "
                            + String.valueOf(proposal.getCompletion()), e));
        }
    }

    /**
     * Gets the completion proposal by the supplied index.
     * 
     * @param index the index.
     * @return the completion proposal.
     */
    public CompletionProposal getProposal(int index) {
        return proposals.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void completionFailure(IProblem problem) {
        ICompilationUnit src = getCompilationUnit();
        IJavaProject javaProject = src.getJavaProject();
        IProject project = javaProject.getProject();

        // undefined type or attempting to complete static members of an
        // unimported type

        if (problem.getID() == IProblem.UndefinedType
                || problem.getID() == IProblem.UndefinedTypeVariable) {
            try {
                SearchPattern pattern = SearchPattern.createPattern(
                        problem.getArguments()[0], IJavaSearchConstants.TYPE,
                        IJavaSearchConstants.DECLARATIONS,
                        SearchPattern.R_REGEXP_MATCH
                                | SearchPattern.R_CASE_SENSITIVE);
                IJavaSearchScope scope = SearchEngine
                        .createJavaSearchScope(new IJavaElement[] { javaProject });
                SearchRequestor requestor = new SearchRequestor();
                SearchEngine engine = new SearchEngine();
                SearchParticipant[] participants = new SearchParticipant[] { SearchEngine
                        .getDefaultSearchParticipant() };
                engine.search(pattern, participants, scope, requestor, null);

                if (!requestor.getMatches().isEmpty()) {
                    imports = Lists.newArrayList();
                    for (SearchMatch match : requestor.getMatches()) {
                        if (SearchMatch.A_ACCURATE != match.getAccuracy())
                            continue;

                        IJavaElement element = (IJavaElement) match
                                .getElement();
                        String name = null;

                        switch (element.getElementType()) {
                            case IJavaElement.TYPE:
                                IType type = (IType) element;
                                if (Flags.isPublic(type.getFlags())) {
                                    name = type.getFullyQualifiedName();
                                }
                                break;
                            case IJavaElement.METHOD:
                            case IJavaElement.FIELD:
                                name = ((IType) element.getParent())
                                        .getFullyQualifiedName()
                                        + '.'
                                        + element.getElementName();
                                break;
                            default:
                                break;
                        }

                        if (null != name) {
                            name = name.replace('$', '.');
                            if (!JavaSourceService.getInstance()
                                    .isImportExcluded(project, name)) {
                                imports.add(name);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        IResource resource = src.getResource();
        String relativeName = resource.getProjectRelativePath().toString();

        if (new String(problem.getOriginatingFileName()).endsWith(relativeName)) {
            String fileName = resource.getLocation().toString();

            // ignore the problem if a temp file is being used and the problem
            // is that the type needs to be defined in its own file.
            // if (IProblem.PublicClassMustMatchFileName == problem.getID()
            // && -1 != fileName.indexOf("__eclim_temp_")) {
            // return;
            // }

            int pos[] = null;

            try {
                pos = new FileObject(fileName).getLineColumn(problem
                        .getSourceStart());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                pos = new int[] { problem.getSourceLineNumber(), 0 };
            }

            error = EclipseResourceUtil.wrapProblemAsMap(problem.getMessage(),
                    problem.getSourceStart(), problem.getSourceEnd(), fileName,
                    pos[0], pos[1], problem.isError() ? 2 : 1);
        }
    }

    public List<String> getImports() {
        return imports;
    }

    public Map<String, Object> getError() {
        return error;
    }
}
