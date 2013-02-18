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

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

/**
 * Utility methods for working with IMethod elements.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
public class MethodUtil {

    private static final String VARARGS = "...";

    /**
     * Creates an new MethodUtil instance.
     */
    private MethodUtil() {
    }

    /**
     * Determines if the supplied types contains the specified method.
     * 
     * @param typeInfo the type info.
     * @param method the method.
     * @return true if the type contains the method, false otherwise.
     * @throws Exception
     */
    public static boolean containsMethod(TypeInfo typeInfo, IMethod method)
            throws Exception {
        return null != getMethod(typeInfo, method) ? true : false;
    }

    /**
     * Gets the method from the supplied type that matches the signature of the
     * specified method.
     * 
     * @param typeInfo
     * @param method
     * @return
     * @throws Exception
     */
    public static IMethod getMethod(TypeInfo typeInfo, IMethod method)
            throws Exception {
        IType type = typeInfo.getType();
        String signature = getMinimalMethodSignature(method, typeInfo);

        if (method.isConstructor()) {
            signature = signature.replaceFirst(method.getDeclaringType()
                    .getElementName(), type.getElementName());
        }

        IMethod[] methods = type.getMethods();

        for (int i = 0; i < methods.length; i++) {
            String methodSig = getMinimalMethodSignature(methods[i], typeInfo);

            if (methodSig.equals(signature)) {
                return methods[i];
            }
        }

        return null;
    }

    /**
     * Retrieves the method which follows the supplied method in the specified
     * type.
     * 
     * @param type the type.
     * @param method the method.
     * @return the method declared after the supplied method.
     * @throws Exception
     */
    public static IMethod getMethodAfter(IType type, IMethod method)
            throws Exception {
        if (null == type || null == method)
            return null;

        // get the method after the sibling.
        IMethod[] all = type.getMethods();
        for (int i = 0; i < all.length; i++) {
            if (all[i].equals(method) && i < all.length - 1) {
                return all[i + 1];
            }
        }
        return null;
    }

    /**
     * Gets a string representation of the supplied method's signature.
     * 
     * @param method the method.
     * @param typeInfo the type info.
     * @return the signature for the method.
     * @throws Exception
     */
    public static String etMethodSignature(IMethod method, TypeInfo typeInfo)
            throws Exception {
        int flags = method.getFlags();
        StringBuilder buffer = new StringBuilder();
        if (method.getDeclaringType().isInterface()) {
            buffer.append("public ");
        } else {
            buffer.append(Flags.isPublic(method.getFlags()) ? "public "
                    : "protected ");
        }

        buffer.append(Flags.isAbstract(flags) ? "abstract " : "");

        if (!method.isConstructor()) {
            String name = Signature.getSignatureSimpleName(method
                    .getReturnType());
            buffer.append(TypeUtil.replaceTypeParams(name, typeInfo)).append(
                    ' ');
        }

        buffer.append(method.getElementName()).append("(")
                .append(getMethodParameters(method, typeInfo, true))
                .append(")");

        String[] exceptions = method.getExceptionTypes();
        if (0 < exceptions.length) {
            buffer.append("\n\tthrows ").append(getMethodThrows(method));
        }
        return buffer.toString();
    }

    /**
     * Gets just enough of a method's signature that it can be distiguished from
     * the other methods.
     * 
     * @param method the method.
     * @param typeInfo the type info.
     * @return the signature of the method.
     * @throws Exception
     */
    public static String getMinimalMethodSignature(IMethod method,
            TypeInfo typeInfo) throws Exception {
        StringBuilder buffer = new StringBuilder();
        buffer.append(method.getElementName()).append("(")
                .append(getMethodParameters(method, typeInfo, false))
                .append(')');
        return buffer.toString();
    }

    /**
     * Gets the supplied method's parameter types and optionally names, in a
     * comma separated string.
     * 
     * @param method the method.
     * @param typeInfo the type info.
     * @param includeNames true to include the parameter names in the string.
     * @return the parameters as a string.
     * @throws Exception
     */
    public static String getMethodParameters(IMethod method, TypeInfo typeInfo,
            boolean includeNames) throws Exception {
        StringBuilder buffer = new StringBuilder();
        String[] paramTypes = method.getParameterTypes();
        String[] paramNames = null;

        if (includeNames) {
            paramNames = method.getParameterNames();
        }

        boolean varargs = false;

        for (int i = 0; i < paramTypes.length; i++) {
            if (0 != i)
                buffer.append(includeNames ? ", " : ",");

            String type = paramTypes[i];

            // check for varargs.
            if (i == paramTypes.length - 1
                    && Signature.getTypeSignatureKind(type) == Signature.ARRAY_TYPE_SIGNATURE
                    && Flags.isVarargs(method.getFlags())) {
                type = Signature.getElementType(paramTypes[i]);
                varargs = true;
            }

            type = Signature.getSignatureSimpleName(type);
            type = type.replaceAll("\\?\\s+extends\\s+", "");
            type = TypeUtil.replaceTypeParams(type, typeInfo);

            buffer.append(type);

            if (varargs)
                buffer.append(VARARGS);

            if (includeNames)
                buffer.append(' ').append(paramNames[i]);
        }
        return buffer.toString();
    }

    /**
     * Gets the list of thrown exceptions as a comma separated string.
     * 
     * @param method the method.
     * @return the thrown exceptions or null if none.
     * @throws Exception
     */
    public static String getMethodThrows(IMethod method) throws Exception {
        String[] exceptions = method.getExceptionTypes();
        if (0 < exceptions.length) {
            StringBuilder buffer = new StringBuilder();

            for (int i = 0; i < exceptions.length; i++) {
                if (0 != i)
                    buffer.append(", ");

                buffer.append(Signature.getSignatureQualifier(exceptions[i]));
            }
        }
        
        return null;
    }
}