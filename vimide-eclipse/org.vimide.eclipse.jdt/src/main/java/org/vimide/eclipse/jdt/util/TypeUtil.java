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
package org.vimide.eclipse.jdt.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Utility methods for working with IType.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class TypeUtil {

    /**
     * Gets the type at the supplied offset, which will either be the primary
     * type of compilation unit, or an inner class.
     * 
     * @param src the source.
     * @param offset the offset in the source.
     * @return a IType instance.
     * @throws Exception
     */
    public static IType getType(ICompilationUnit src, int offset)
            throws Exception {
        IJavaElement element = src.getElementAt(offset);
        IType type = null;

        // offset outside the class source (Above the package declaration most
        // likely)

        if (null == element) {
            type = ((CompilationUnit) src).getTypeRoot().findPrimaryType();
        } else if (null != element
                && IJavaElement.TYPE == element.getElementType()) {
            type = (IType) element;
        } else {
            element = element.getParent();

            // offset on import statement.

            if (IJavaElement.IMPORT_DECLARATION == element.getElementType()) {
                element = element.getParent();
            }

            // offset on the package declaration or continuation of import ^
            if (IJavaElement.COMPILATION_UNIT == element.getElementType()) {
                element = ((CompilationUnit) element).getTypeRoot()
                        .findPrimaryType();
            }

            type = (IType) element;
        }

        return type;
    }

    /**
     * Gets the signature for the supplied type.
     * 
     * @param typeInfo the TYPEINFO instance.
     * @return the signature.
     * @throws Exception
     */
    public static String getTypeSignature(TypeInfo typeInfo) throws Exception {
        StringBuilder buffer = new StringBuilder();
        IType type = typeInfo.getType();
        int flags = type.getFlags();
        if (Flags.isPublic(flags))
            buffer.append("public");

        buffer.append(type.isClass() ? "class " : "interface ");
        IJavaElement parent = type.getParent();

        if (IJavaElement.TYPE == parent.getElementType())
            buffer.append(type.getParent().getElementName()).append(".");
        else if (IJavaElement.CLASS_FILE == parent.getElementType()) {
            int index = parent.getElementName().indexOf('$');

            if (-1 == index) {
                buffer.append(parent.getElementName().substring(0, index))
                        .append(".");
            }
        }

        buffer.append(type.getElementName());

        String[] params = typeInfo.getTypeParameters();
        String[] args = typeInfo.getTypeArguments();
        if (null != params && 0 < params.length && null != args
                && 0 < args.length) {
            buffer.append('<');

            for (int i = 0; i < args.length; i++) {
                if (i > 0)
                    buffer.append(',');
                buffer.append(args[i]);
            }
            buffer.append('>');
        }
        return buffer.toString();
    }

    /**
     * Determines if any of the super of the supplied type contains the supplied
     * method and if so return that super type.
     * 
     * @param type the type.
     * @param method the method.
     * @return the super type that contains the method, or null if none.
     * @throws Exception
     */
    public static TypeInfo getSuperTypeContainingMethod(IType type,
            IMethod method) throws Exception {
        TypeInfo[] types = getSuperTypes(type);

        for (TypeInfo info : types) {
            IMethod[] methods = info.getType().getMethods();
            for (IMethod m : methods) {
                if (m.isSimilar(method)) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * Determines if any of the super types of the supplied type contains a
     * method with the supplied signature and if so return that super type.
     * 
     * @param type the type.
     * @param signature the method signature.
     * @return the super type that contains the method, or null if none.
     * @throws Exception
     */
    public static Object[] getSuperTypeContainingMethod(IType type,
            String signature) throws Exception {
        TypeInfo[] types = getSuperTypes(type);
        for (TypeInfo info : types) {
            IMethod[] methods = info.getType().getMethods();
            for (IMethod m : methods) {
                String sig = MethodUtil.getMinimalMethodSignature(m, info);
                if (sig.equals(signature)) {
                    return new Object[] { info, m };
                }
            }
        }
        return null;
    }

    /**
     * Recursively gets all superclass and implemented interfaces from the
     * supplied type.
     * 
     * @param type the type.
     * @return array of types.
     * @throws Exception
     */
    public static TypeInfo[] getSuperTypes(IType type) throws Exception {
        return getSuperTypes(type, false);
    }

    /**
     * Recursively gets all superclass and implemented interfaces from the
     * supplied type.
     * 
     * @param type the type.
     * @param returnNotFound whether or not to return handle only instances to
     *            super types that could not be found in the project.
     * @return array of types.
     * @throws Exception
     */
    public static TypeInfo[] getSuperTypes(IType type, boolean returnNotFound)
            throws Exception {
        TypeInfo[] interfaces = getInterfaces(type, returnNotFound);
        TypeInfo[] superClasses = getSuperClasses(type, returnNotFound);
        TypeInfo[] types = new TypeInfo[interfaces.length + superClasses.length];

        System.arraycopy(interfaces, 0, types, 0, interfaces.length);
        System.arraycopy(superClasses, 0, types, interfaces.length,
                superClasses.length);
        return types;
    }

    /**
     * Recursively gets all the super classes for the supplied type.
     * 
     * @param type the type.
     * @return array of super class type.
     * @throws Exception
     */
    public static TypeInfo[] getSuperClasses(IType type) throws Exception {

        return getSuperClasses(type, false);
    }

    /**
     * Recursively gets all the super classes for the super type.
     * 
     * @param type the type
     * @param returnNotFound whether or not to return handle only instances to
     *            super class types that could not be found in the project.
     * @return array of super class types.
     * @throws Exception
     */
    public static TypeInfo[] getSuperClasses(IType type, boolean returnNotFound)
            throws Exception {
        List<TypeInfo> types = Lists.newArrayList();
        getSuperClasses(type, types, returnNotFound, null);

        // add java.lang.Object if not already added.
        IType objectType = type.getJavaProject().findType("java.lang.Object");
        TypeInfo objectTypeInfo = new TypeInfo(objectType, null, null);
        if (!types.contains(objectTypeInfo))
            types.add(objectTypeInfo);

        return types.toArray(new TypeInfo[types.size()]);
    }

    /**
     * Recursively gets all the super classes for the supplied type.
     * 
     * @param type the type.
     * @param superClasses the list to add results to .
     * @param includeNotFound whether or not to include types that were not
     *            found (adds them as handle only IType instances).
     * @param baseType the first type in the recursion stack.
     * @throws Exception
     */
    public static void getSuperClasses(IType type, List<TypeInfo> superClasses,
            boolean includeNotFound, TypeInfo baseType) throws Exception {
        TypeInfo superClassInfo = getSuperClass(type, baseType);
        if (null != superClassInfo) {
            if (null == baseType
                    || baseType.getTypeArguments().length != superClassInfo
                            .getTypeArguments().length) {
                baseType = superClassInfo;
            }
        }

        if (null != superClassInfo && !superClasses.contains(superClassInfo)) {
            superClasses.add(superClassInfo);
            getSuperClasses(superClassInfo.getType(), superClasses,
                    includeNotFound, baseType);
        } else if (null == superClassInfo && includeNotFound) {
            String typeName = type.getSuperclassName();
            if (!Strings.isNullOrEmpty(typeName)) {
                // get a handle only reference to the super class that wasn't
                // found.

                try {
                    IType superClass = type.getType(typeName);
                    superClassInfo = new TypeInfo(superClass, null, null);
                    if (!superClasses.contains(superClassInfo)) {
                        superClasses.add(superClassInfo);
                    }
                } catch (final Exception e) {
                    // don't let the error cause the command to fail.
                }
            }
        }
    }

    /**
     * Gets the super type of the supplied type, if any.
     * 
     * @param type the type to get the super class of.
     * @return the super class type or null if none.
     * @throws Exception
     */
    public static TypeInfo getSuperClass(IType type) throws Exception {
        return getSuperClass(type, null);
    }

    /**
     * Gets the super type of the supplied type, if any.
     * 
     * @param type the type to get the super class of.
     * @param baseType the base class type or null if none.
     * @return the super class type or null if none.
     * @throws Exception
     */
    public static TypeInfo getSuperClass(IType type, TypeInfo baseType)
            throws Exception {
        String superClassSig = type.getSuperclassTypeSignature();
        if (null != superClassSig) {
            String qualifier = Signature.getSignatureQualifier(superClassSig);
            qualifier = (null != qualifier && !qualifier
                    .equals(StringUtils.EMPTY)) ? qualifier + '.'
                    : StringUtils.EMPTY;
            String superClass = qualifier
                    + Signature.getSignatureSimpleName(superClassSig);
            String[] args = Signature.getTypeArguments(superClassSig);
            String[] typeArgs = new String[args.length];

            for (int i = 0; i < args.length; i++) {
                typeArgs[i] = Signature.getSignatureSimpleName(args[i]);
            }

            if (null != baseType
                    && baseType.getTypeArguments().length == typeArgs.length) {
                typeArgs = baseType.getTypeArguments();
            }

            String[][] types = type.resolveType(superClass);

            if (null != types) {
                for (String[] typeInfo : types) {
                    String typeName = typeInfo[0] + '.' + typeInfo[1];
                    IType found = type.getJavaProject().findType(typeName);
                    if (null != found) {
                        ITypeParameter[] params = found.getTypeParameters();
                        String[] typeParams = new String[params.length];
                        for (int i = 0; i < params.length; i++) {
                            typeParams[i] = params[i].getElementName();
                        }

                        return new TypeInfo(found, typeParams, typeArgs);
                    }
                }
            } else {
                IType found = type.getJavaProject().findType(superClass);
                if (null != found) {
                    return new TypeInfo(found, null, typeArgs);
                }
            }
        }
        return null;
    }

    /**
     * Gets an array of directly implemented interfaces for the supplied type,
     * if any.
     * 
     * @param type the type to get the interfaces of.
     * @return array of interface type.
     * @throws Exception
     */
    public static IType[] getSuperInterfaces(IType type) throws Exception {
        String[] parents = type.getSuperInterfaceNames();
        List<IType> interfaces = Lists.newArrayList();
        for (String parent : parents) {
            String[][] types = type.resolveType(parent);
            if (null != types) {
                for (String[] typeInfo : types) {
                    String typeName = typeInfo[0] + '.' + typeInfo[1];
                    IType found = type.getJavaProject().findType(typeName);
                    if (null != found)
                        interfaces.add(found);
                }
            } else {
                IType found = type.getJavaProject().findType(parent);
                if (null != found)
                    interfaces.add(found);
            }
        }

        return interfaces.toArray(new IType[interfaces.size()]);
    }

    /**
     * Recursively gets all the implemented interfaces for the supplied type.
     * 
     * @param type the type.
     * @return array of the interface types.
     * @throws Exception
     */
    public static TypeInfo[] getInterfaces(IType type) throws Exception {
        return getInterfaces(type, false);
    }

    /**
     * Recursively gets all the implemented interfaces for the supplied type.
     * 
     * @param type the type.
     * @param returnNotFound whether or not to return handle only instances to
     *            super interface types that could not be found in the project.
     * @return array of interface types.
     * @throws Exception
     */
    public static TypeInfo[] getInterfaces(IType type, boolean returnNotFound)
            throws Exception {
        List<TypeInfo> types = Lists.newArrayList();
        getInterfaces(type, types, returnNotFound, null);

        return types.toArray(new TypeInfo[types.size()]);
    }

    /**
     * Recursively gets the interfaces implemented by the supplied type.
     * 
     * @param type the type.
     * @param interfaces the list to add results to.
     * @param includeNotFound whether or not to include types that were not
     *            found (adds them as handle only IType instances)
     * @param baseType the first type in the recursion stack.
     * @throws Exception
     */
    public static void getInterfaces(IType type, List<TypeInfo> interfaces,
            boolean includeNotFound, TypeInfo baseType) throws Exception {
        // directory implemented interfaces.
        String[] parentSigs = type.getSuperInterfaceTypeSignatures();

        for (String parentSig : parentSigs) {
            String parent = Signature.getSignatureSimpleName(parentSig);
            String[] args = Signature.getTypeArguments(parentSig);
            String[] typeArgs = new String[args.length];

            for (int i = 0; i < args.length; i++) {
                typeArgs[i] = Signature.getSignatureSimpleName(args[i]);
            }

            if (null != baseType
                    && baseType.getTypeArguments().length == typeArgs.length) {
                typeArgs = baseType.getTypeArguments();
            }

            IType found = null;
            String[][] types = type.resolveType(parent);

            if (null != types) {
                for (String[] typeInfo : types) {
                    String typeName = typeInfo[0] + '.' + typeInfo[1];
                    found = type.getJavaProject().findType(typeName);
                }
            } else {
                found = type.getJavaProject().findType(parent);
            }

            if (null != found) {
                ITypeParameter[] params = found.getTypeParameters();
                String[] typeParams = new String[params.length];

                for (int i = 0; i < params.length; i++) {
                    typeParams[i] = params[i].getElementName();
                }

                TypeInfo typeInfo = new TypeInfo(found, typeParams, typeArgs);

                if (!interfaces.contains(typeInfo)) {
                    interfaces.add(typeInfo);

                    if (null == baseType
                            || baseType.getTypeArguments().length != typeInfo
                                    .getTypeArguments().length) {
                        baseType = typeInfo;
                    }

                    getInterfaces(found, interfaces, includeNotFound, baseType);
                }
            } else if (null == found && includeNotFound) {
                String typeName = parent;

                if (!Strings.isNullOrEmpty(typeName)) {
                    // get a handle only reference to the super class that
                    // wasn't found.

                    try {
                        found = type.getType(typeName);
                        TypeInfo typeInfo = new TypeInfo(found, null, typeArgs);
                        if (!interfaces.contains(typeInfo))
                            interfaces.add(typeInfo);
                    } catch (final Exception e) {
                        // don't let the error cause the command to fail.
                    }
                }
            } else if (null == found) {
                // logging
                System.out
                        .println("Warning: unable to resolve implemented interface '"
                                + parent
                                + "' for '"
                                + type.getFullyQualifiedName() + "'");
            }
        }

        // indirectly implemented parents.
        TypeInfo superClassInfo = getSuperClass(type);
        if (null != superClassInfo) {
            getInterfaces(superClassInfo.getType(), interfaces,
                    includeNotFound, superClassInfo);
        }
    }

    /**
     * If the supplied type represents a generic param type, then replace it
     * with the properly concrete type from the supplied type info.
     * 
     * @param type the possibly generic param type.
     * @param typeInfo the type info.
     * @return the concrete type.
     * @throws Exception
     */
    public static String replaceTypeParams(String type, TypeInfo typeInfo)
            throws Exception {
        if (null != typeInfo) {
            String[] params = typeInfo.getTypeParameters();
            String[] args = typeInfo.getTypeArguments();
            if (null != params && params.length == args.length) {
                for (int i = 0; i < params.length; i++) {
                    type = type.replaceAll("\\b" + params[i] + "\\b", args[i]);
                }
            }
        }
        return type;
    }

    /**
     * Converts the supplied the signature with generic information to the most
     * basic type it supports.
     * 
     * @param type the parent type.
     * @param typeSignature the type signature.
     * @return the base type.
     * @throws Exception
     */
    public static String getBaseTypeFromGeneric(IType type, String typeSignature)
            throws Exception {
        int arrayCount = Signature.getArrayCount(typeSignature);

        if (arrayCount > 0) {
            for (int i = 0; i < arrayCount; i++) {
                typeSignature = Signature.getElementType(typeSignature);
            }
        }

        String result = null;
        ITypeParameter param = type.getTypeParameter(Signature
                .getSignatureSimpleName(typeSignature));

        if (param.exists()) {
            result = param.getBounds()[0];
        } else {
            result = Signature.getSignatureSimpleName(Signature
                    .getTypeErasure(typeSignature));
        }

        if (arrayCount > 0) {
            for (int i = 0; i < arrayCount; i++) {
                result = result + "[]";
            }
        }

        return result;
    }
}
