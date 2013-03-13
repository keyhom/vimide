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
package org.vimide.eclipse.jdt.manipulation;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedMethodsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.osgi.util.NLS;
import org.vimide.eclipse.jdt.JavaSourceFacade;
import org.vimide.eclipse.jdt.JdtMessages;
import org.vimide.eclipse.jdt.util.MethodUtil;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Manipulator used to build a tree of methods that have or can be
 * implemented/overriden by the supplied file according the interfaces/parent
 * class it implements/extends.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@SuppressWarnings("restriction")
public class ImplementationManipulator {

    /**
     * Accepts the supplied bean and make a manipulation.
     * 
     * @param bean the specific bean.
     * @throws Exception
     */
    public void accept(final ImplementationBean bean) throws Exception {
        // make the methods to a set.
        if (!Strings.isNullOrEmpty(bean.getSuperTypeName())) {
            insertMethods(bean);
        }
    }

    /**
     * Gets the implementation result containing super types and their methods
     * that can be added to the supplied source.
     * 
     * @param src the source file.
     * @param type the type in the source that methods would be modified.
     * @return an implementation result containing.
     * @throws Exception
     */
    public ImplementationResult getResult(ICompilationUnit src, IType type)
            throws Exception {
        List<IMethodBinding> overridable = getOverridableMethods(src, type);
        return getResult(type.getFullyQualifiedName(), overridable);
    }

    /**
     * Gets the implementation result containing super types and their methods
     * that can be added to the supplied source.
     * 
     * @param name the name of the type in the source that methods would be
     *            added to.
     * @param methods a list of IMethodBinding representing the available
     *            methods.
     * @return an implementation result containing.
     * @throws Exception
     */
    protected ImplementationResult getResult(String name,
            List<IMethodBinding> methods) throws Exception {
        List<ImplementationType> results = Lists.newArrayList();
        List<String> overrideMethods = null;
        ITypeBinding curTypeBinding = null;

        for (IMethodBinding methodBinding : methods) {
            ITypeBinding typeBinding = methodBinding.getDeclaringClass();
            if (typeBinding != curTypeBinding) {
                if (overrideMethods != null && overrideMethods.size() > 0) {
                    results.add(createImplType(curTypeBinding, overrideMethods));
                }
                overrideMethods = Lists.newArrayList();
            }

            curTypeBinding = typeBinding;
            overrideMethods.add(getMethodBindingSignature(methodBinding));
        }

        if (null != overrideMethods && !overrideMethods.isEmpty()) {
            results.add(createImplType(curTypeBinding, overrideMethods));
        }

        return new ImplementationResult(name, results);
    }

    private ImplementationType createImplType(ITypeBinding typeBinding,
            List<String> overridable) {
        String signature = (typeBinding.isInterface() ? "interface " : "class ")
                + typeBinding.getName().replaceAll("#RAM", "");
        return new ImplementationType(typeBinding.getPackage().getName(),
                signature, overridable.toArray(new String[overridable.size()]));
    }

    private void insertMethods(final ImplementationBean bean) throws Exception {
        final ICompilationUnit src = bean.getCompilationUnit();
        final IType type = bean.getType();
        final String[] methods = bean.getMethodNames();

        Set<String> chosen = null;

        if (null != methods && methods.length > 0) {
            chosen = Sets.newHashSet(methods);
        }

        int pos = -1;
        int len = src.getBuffer().getLength();

        IJavaElement sibling = getSibling(type);

        if (null != sibling) {
            pos = ((ISourceReference) sibling).getSourceRange().getOffset();
        }

        IWorkspaceRunnable op = getImplOperation(bean, chosen, sibling, pos);

        if (null != op) {
            String lineDelim = src.findRecommendedLineSeparator();
            IImportDeclaration[] imports = src.getImports();
            int importsEnd = -1;
            if (imports.length > 0) {
                ISourceRange last = imports[imports.length - 1]
                        .getSourceRange();
                importsEnd = last.getOffset() + last.getLength()
                        + lineDelim.length();
            }

            op.run(null);

            // an operation.getResultingEdit() would be nice here, but we'll
            // make do w/ what we got and caculate our own edit offset / length
            // combo so we can format the new code.
            int offset = pos != -1 ? pos : (len - 1 - lineDelim.length());
            int newLen = src.getBuffer().getLength();
            int length = newLen - len - 1;

            // the change in length may include newly added imported, so handle
            // that as best we can.
            int importLenChange = 0;
            imports = src.getImports();
            if (importsEnd != -1) {
                ISourceRange last = imports[imports.length - 1]
                        .getSourceRange();
                importLenChange = last.getOffset() + last.getLength()
                        + lineDelim.length() - importsEnd;
            } else if (imports.length > 0) {
                ISourceRange first = imports[0].getSourceRange();
                ISourceRange last = imports[imports.length - 1]
                        .getSourceRange();
                importLenChange = last.getOffset() + last.getLength()
                        + (lineDelim.length() * 2) - first.getOffset();
            }

            offset += importLenChange;
            length -= importLenChange;

            JavaSourceFacade.format(src, offset, length);
        }
    }

    /**
     * Gets the operation used to add the requested methods.
     * 
     * @param bean the javabean of implmenetation / overriden.
     * @param chosen a set containing method signatures to add or null to add
     *            all methods from the chosen super type.
     * @param sibling the sibling to insert the methods before.
     * @param pos the position of the sibling.
     * @return a IWorkspaceRunnable.
     */
    protected IWorkspaceRunnable getImplOperation(ImplementationBean bean,
            Set<String> chosen, IJavaElement sibling, int pos) throws Exception {
        IType type = bean.getType();
        RefactoringASTParser parser = new RefactoringASTParser(
                ASTProvider.SHARED_AST_LEVEL);
        CompilationUnit cu = parser.parse(type.getCompilationUnit(), true);
        ITypeBinding typeBinding = ASTNodes.getTypeBinding(cu, type);

        String superType = bean.getSuperTypeName();
        List<IMethodBinding> overridable = getOverridableMethods(cu,
                typeBinding);
        List<IMethodBinding> override = Lists.newArrayList();

        for (IMethodBinding binding : overridable) {
            ITypeBinding declBinding = binding.getDeclaringClass();
            String fqn = declBinding.getQualifiedName().replaceAll("<.*?>", "");
            if (fqn.equals(superType) && isClosen(chosen, binding)) {
                override.add(binding);
            }
        }

        if (!override.isEmpty()) {
            return new AddUnimplementedMethodsOperation(cu, typeBinding,
                    override.toArray(new IMethodBinding[override.size()]), pos,
                    true, true, true);
        }
        return null;
    }

    /**
     * Determines if the supplied IMethodBinding is in the set of chosen methods
     * to insert.
     * 
     * @param chosen the set of chosen method signatures.
     * @param binding the binding to check.
     * @return true if the method is in the chosen set, false otherwise.
     */
    protected boolean isClosen(Set<String> chosen, IMethodBinding binding) {
        return chosen == null
                || chosen.contains(getMethodBindingShortCallSignature(binding));
    }

    private String getMethodBindingSignature(IMethodBinding binding) {
        return binding
                .toString()
                .trim()
                .replaceAll("\\bjava\\.lang\\.", "")
                .replaceAll("\\s+throws\\s+.*", "")
                .replaceFirst("\\w+\\s*\\(.?\\)",
                        getMethodBindingCallSignature(binding));
    }

    private String getMethodBindingShortCallSignature(IMethodBinding binding) {
        return getMethodBindingCallSignature(binding).replaceAll("<.*?>", "");
    }

    private String getMethodBindingCallSignature(IMethodBinding binding) {
        ITypeBinding[] paramTypes = binding.getParameterTypes();
        String[] params = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            params[i] = paramTypes[i].getQualifiedName()
                    .replaceAll("\\bjava\\.lang\\.", "").replaceAll("#RAW", "");
        }
        return binding.getName() + '(' + StringUtils.join(params, ',') + ')';
    }

    /**
     * Gets a list of overridable IMethodBindings.
     * 
     * @param src the source file.
     * @param type the type within the source file.
     * @return list of IMethodBinding.
     * @throws Exception
     */
    protected List<IMethodBinding> getOverridableMethods(ICompilationUnit src,
            IType type) throws Exception {
        RefactoringASTParser parser = new RefactoringASTParser(
                ASTProvider.SHARED_AST_LEVEL);
        CompilationUnit cu = parser.parse(type.getCompilationUnit(), true);
        ITypeBinding typeBinding = ASTNodes.getTypeBinding(cu, type);
        return getOverridableMethods(cu, typeBinding);
    }

    /**
     * Gets a list of overridable IMethodBindings.
     * 
     * @param cu AST CompilationUnit.
     * @param typeBinding the binding of the type with the compilation unit.
     * @return list of IMethodBinding.
     * @throws Exception
     */
    protected List<IMethodBinding> getOverridableMethods(CompilationUnit cu,
            ITypeBinding typeBinding) throws Exception {
        if (!typeBinding.isClass()) {
            throw new IllegalArgumentException(NLS.bind(
                    JdtMessages.type_not_a_class,
                    typeBinding.getQualifiedName()));
        }

        IPackageBinding packageBinding = typeBinding.getPackage();
        IMethodBinding[] methods = StubUtility2.getOverridableMethods(
                cu.getAST(), typeBinding, false);
        List<IMethodBinding> overridable = Lists.newArrayList();
        for (IMethodBinding methodBinding : methods) {
            if (Bindings.isVisibleInHierarchy(methodBinding, packageBinding)) {
                overridable.add(methodBinding);
            }
        }
        return overridable;
    }

    private IJavaElement getSibling(IType type) throws Exception {
        IJavaElement sibling = null;

        // insert after last method.
        IMethod[] methods = type.getMethods();
        if (methods.length > 0) {
            sibling = MethodUtil.getMethodAfter(type,
                    methods[methods.length - 1]);
        }

        // insert before inner classes.
        if (null == sibling) {
            IType[] types = type.getTypes();
            // find the first non-enum type.
            for (int i = 0; i < types.length; i++) {
                if (!types[i].isEnum()) {
                    sibling = types[i];
                    break;
                }
            }
        }
        return sibling;
    }

}
// vim:ft=java
