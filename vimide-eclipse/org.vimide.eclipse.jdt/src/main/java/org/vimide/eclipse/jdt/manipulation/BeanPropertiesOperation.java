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

import java.lang.reflect.Modifier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.codemanipulation.AddGetterSetterOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.GetterSetterUtil;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.text.edits.TextEdit;
import org.vimide.eclipse.jdt.JavaSourceFacade;
import org.vimide.eclipse.jdt.util.ASTUtil;
import org.vimide.eclipse.jdt.util.MethodUtil;

/**
 * Operations used to generate java bean property methods (getters / setters).
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@SuppressWarnings("restriction")
public class BeanPropertiesOperation {

    /**
     * Accepts a BeanProperties object and make.
     * 
     * @param beanProperties the bean properties object.
     * @throws Exception
     */
    public void accept(BeanProperties beanProperties) throws Exception {
        final ICompilationUnit src = beanProperties.getCompilationUnit();
        final CodeGenerationSettings settings = JavaPreferencesSettings
                .getCodeGenerationSettings(src.getJavaProject());
        settings.createComments = true;
        final IType type = beanProperties.getEnclosingType();
        IField[] fields = type.getFields();

        if (null != type) {
            for (String fieldString : beanProperties.getFields()) {
                IField field = type.getField(fieldString);
                if (null != field) {
                    boolean isArray = Signature.getArrayCount(field
                            .getTypeSignature()) > 0;

                    IField getter = null, setter = null;

                    if (beanProperties.containsGetter())
                        getter = field;
                    if (beanProperties.containsSetter())
                        setter = field;

                    int methodType = getter != null ? BeanProperties.TYPE_GET
                            : BeanProperties.TYPE_SET;
                    // edge case to prevent insert setter before getter if
                    // getter already exists.
                    if (null != getter && null != setter
                            && null != GetterSetterUtil.getGetter(field)) {
                        methodType = BeanProperties.TYPE_SET;
                    }

                    IJavaElement sibling = getSibling(type, fields, field,
                            methodType);
                    insertMethods(src, type, sibling, settings, getter, setter);

                    if (isArray && beanProperties.isIndexed()) {
                        insertIndexMethods(src, type, fields, settings, getter,
                                setter);
                    }
                }
            }
        }

        // done with all.
    }

    protected void insertIndexMethods(ICompilationUnit src, IType type,
            IField[] fields, CodeGenerationSettings settings, IField getter,
            IField setter) throws Exception {
        // eclipse doesn't natively support indexed accessors, so this method
        // runs some regexes on the getter/setter stubs to generate indexed
        // version.

        if (null != getter) {
            IMethod existing = getBeanMethod(type, getter,
                    BeanProperties.TYPE_GET_INDEX);
            if (null == existing) {
                IJavaElement sibling = getSibling(type, fields, getter,
                        BeanProperties.TYPE_GET_INDEX);
                String name = GetterSetterUtil.getGetterName(getter, null);
                String stub = GetterSetterUtil.getGetterStub(getter, name,
                        settings.createComments, Modifier.PUBLIC);
                stub = stub.replaceFirst("\n(\\s*\\*\\s*@return)",
                        "\n * @param index the index to get\n$1");
                stub = stub.replaceFirst("\\[\\]\\s*(" + name + "\\s*\\()\\)",
                        " $1int index)");
                stub = stub.replaceFirst("(return\\s+\\w+)(.*?;)",
                        "$1[index]$2");

                IMethod inserted = type
                        .createMethod(stub, sibling, false, null);
                // format the inserted method according to the user's
                // preferences.
                ISourceRange range = inserted.getSourceRange();
                JavaSourceFacade.format(src, range.getOffset(),
                        range.getLength());
            }
        }

        if (null != setter) {
            IMethod existing = getBeanMethod(type, setter,
                    BeanProperties.TYPE_SET_INDEX);
            if (null == existing) {
                IJavaElement sibling = getSibling(type, fields, setter,
                        BeanProperties.TYPE_SET_INDEX);
                String name = GetterSetterUtil.getGetterName(setter, null);
                String stub = GetterSetterUtil.getGetterStub(setter, name,
                        settings.createComments, Modifier.PUBLIC);
                stub = stub.replaceFirst("\n(\\s*\\*\\s*@param)",
                        "\n * @param index the index to set\n$1");
                stub = stub.replaceFirst("(" + name
                        + "\\s*\\()(.*?)\\[\\](\\s*.*?)\\)",
                        "$1int index, $2$3)");
                stub = stub.replaceFirst("(\\w+)(\\s*=\\s*)", "$1[index]$2");

                IMethod inserted = type
                        .createMethod(stub, sibling, false, null);
                // format the inserted method according to the user's
                // preferences.
                ISourceRange range = inserted.getSourceRange();
                JavaSourceFacade.format(src, range.getOffset(),
                        range.getLength());
            }
        }
    }

    protected void insertMethods(ICompilationUnit src, IType type,
            IJavaElement sibling, CodeGenerationSettings settings,
            IField getter, IField setter) throws CoreException {
        AddGetterSetterOperation op = new AddGetterSetterOperation(type,
                getter != null ? new IField[] { getter }
                        : BeanProperties.NO_FIELDS,
                setter != null ? new IField[] { setter }
                        : BeanProperties.NO_FIELDS, BeanProperties.NO_FIELDS,
                ASTUtil.getCompilationUnit(src), null, sibling, settings, true,
                true);
        op.run(null);

        TextEdit edit = op.getResultingEdit();
        if (null != edit) {
            JavaSourceFacade.format(src, edit.getOffset(), edit.getLength());
        }
    }

    protected IJavaElement getSibling(IType type, IField[] fields,
            IField field, int methodType) throws Exception {
        IMethod siblingMethod = null;
        int siblingType = BeanProperties.TYPE_GET;
        // first try other methods for the same field.
        for (int i = BeanProperties.TYPE_GET; i <= BeanProperties.TYPE_GET_INDEX; i++) {
            IMethod method = getBeanMethod(type, field, i);
            if (null != method) {
                siblingMethod = method;
                siblingType = i;
            }
        }

        if (null != siblingMethod) {
            if (methodType < siblingType) {
                siblingMethod = MethodUtil.getMethodAfter(type, siblingMethod);
            }

            if (null != siblingMethod) {
                return siblingMethod;
            }
            return getFirstInnerType(type);
        }

        int index = 0;
        for (index = 0; index < fields.length; index++) {
            if (field.getElementName().equals(fields[index])) {
                break;
            }
        }

        // insert before the next property's bean methods, if there are other
        // properties.
        if (1 < fields.length && (index + 1) < fields.length) {
            IMethod method = null;
            for (int i = index + 1; method == null && i < fields.length; i++) {
                IField property = (IField) fields[i];
                method = getBeanMethod(type, property, false);
            }

            if (null != method)
                return method;
        }

        // insert after previous property's bean methods, if there are other
        // properties.
        if (1 < fields.length && 0 < index) {
            IMethod method = null;
            for (int i = index - 1; method == null && i >= 0; i--) {
                IField property = (IField) fields[i];
                method = getBeanMethod(type, property, true);
            }

            if (null != method) {
                method = MethodUtil.getMethodAfter(type, method);
                if (null != method) {
                    return method;
                }
            }
        }
        return getFirstInnerType(type);
    }

    protected IJavaElement getFirstInnerType(IType type) throws Exception {
        // insert before inner classes.
        IType[] types = type.getTypes();
        // find the first non-enum type.
        for (int i = 0; i < types.length; i++) {
            if (!types[i].isEnum()) {
                return types[i];
            }
        }
        return null;
    }

    protected IMethod getBeanMethod(IType type, IField field, int methodType)
            throws Exception {
        String name = Signature
                .getSignatureSimpleName(field.getTypeSignature());
        boolean isArray = Signature.getArrayCount(field.getTypeSignature()) > 0;

        String signature = null;
        switch (methodType) {
            case BeanProperties.TYPE_GET:
                return GetterSetterUtil.getGetter(field);
            case BeanProperties.TYPE_GET_INDEX:
                if (isArray) {
                    signature = GetterSetterUtil.getGetterName(field, null)
                            + "(int)";
                }
                break;
            case BeanProperties.TYPE_SET:
                return GetterSetterUtil.getSetter(field);
            case BeanProperties.TYPE_SET_INDEX:
                if (isArray) {
                    signature = GetterSetterUtil.getSetterName(field, null)
                            + "(int, " + name + ")";
                }
        }

        if (null != signature) {
            IMethod[] methods = type.getMethods();
            for (int i = 0; i < methods.length; i++) {
                String sig = MethodUtil.getMinimalMethodSignature(methods[i],
                        null);
                if (sig.equals(signature)) {
                    return methods[i];
                }
            }
        }
        return null;
    }

    protected IMethod getBeanMethod(IType type, IField field, boolean last)
            throws JavaModelException {
        IMethod result = null;
        boolean isArray = Signature.getArrayCount(field.getTypeSignature()) > 0;

        // regular getter.
        IMethod method = GetterSetterUtil.getGetter(field);
        if (null != method && method.exists() && !last) {
            return method;
        } else if (null != method && method.exists()) {
            result = method;
        }

        // index getter.
        if (isArray) {
            method = type.getMethod(
                    GetterSetterUtil.getGetterName(field, null),
                    BeanProperties.INT_ARGUMENT);
            if (method.exists() && !last) {
                return method;
            } else if (method.exists()) {
                result = method;
            }
        }

        // regular setter.
        method = GetterSetterUtil.getSetter(field);
        if (null != method && method.exists() && !last) {
            return method;
        } else if (null != method && method.exists()) {
            result = method;
        }

        // index setter.
        if (!isArray) {
            String elementType = Signature.getElementType(field
                    .getTypeSignature());
            method = type.getMethod(
                    GetterSetterUtil.getSetterName(field, null), new String[] {
                            BeanProperties.INT_SIGNATURE, elementType });
            if (method.exists()) {
                result = method;
            }
        }
        return result;
    }

}
