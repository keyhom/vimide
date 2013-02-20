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
package org.vimide.eclipse.jdt.service;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vimide.eclipse.jdt.VimideJdtPlugin;
import org.vimide.eclipse.jdt.util.ASTUtil;
import org.vimide.eclipse.jdt.util.EclipseJdtUtil;
import org.vimide.eclipse.jdt.util.MethodUtil;
import org.vimide.eclipse.jdt.util.TypeInfo;
import org.vimide.eclipse.jdt.util.TypeUtil;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The service implementation for the java source functions.
 * 
 * @author keyhom (keyhom.c@gmail.com)
 */
@SuppressWarnings("restriction")
public class JavaSourceService extends JavaBaseService {

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(JavaSourceService.class.getName());

    static final Pattern THROWS_PATTERN = Pattern
            .compile("\\s*[a-zA-Z0-9._]*\\.(\\w*)($|\\s.*)");

    static final String INHERIT_DOC = "{" + TagElement.TAG_INHERITDOC + "}";

    /**
     * Lazy singleton holder.
     * 
     * @author keyhom (keyhom.c@gmail.com)
     */
    private static class SingletonHolder {
        static final JavaSourceService instance = new JavaSourceService();
    }

    /**
     * Gets the singelton instance.
     * 
     * @return singleton.
     */
    public static JavaSourceService getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Gets the formatting options.
     * 
     * @return formatting options as map.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getOptions(final IJavaProject project) {
        if (null == project) {
            return null;
        }

        // take default formatting options.
        final Map<String, String> options = DefaultCodeFormatterConstants
                .getEclipseDefaultSettings();
        // fill with the project specific options.
        options.putAll(project.getOptions(true));
        // fill with the default options.
        options.putAll(getVimideDefaultOptions());

        return options;
    }

    protected Map<String, String> getVimideDefaultOptions() {
        final Map<String, String> options = Maps.newHashMap();
        options.put(DefaultCodeFormatterConstants.FORMATTER_USE_ON_OFF_TAGS,
                DefaultCodeFormatterConstants.TRUE);
        options.put(
                DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_NEW_LINE_FOR_PARAMETER,
                DefaultCodeFormatterConstants.FALSE);
        options.put(
                DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT,
                DefaultCodeFormatterConstants.TRUE);
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, "space");
        options.put(
                DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH,
                DefaultCodeFormatterConstants.TRUE);
        options.put(
                DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_ENUM_CONSTANT,
                "48");
        options.put(
                DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_SUPERINTERFACES_IN_ENUM_DECLARATION,
                "48");
        options.put(
                DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
                "49");
        return options;
    }

    /**
     * Formats the supplied source with the specific formatter options.
     * 
     * @param src the source to format.
     * @param options the options to format.
     * @param bOffset the begin offset of the source.
     * @param eOffset the end offset of the source.
     * @return the document by being formatted.
     * @throws CoreException
     */
    public IDocument format(ICompilationUnit src, Map<String, String> options,
            int bOffset, int eOffset) throws CoreException {
        if (null == src)
            throw new CoreException(new Status(IStatus.ERROR,
                    VimideJdtPlugin.PLUGIN_ID, "Illegal src."));

        try {
            final String source = src.getBuffer().getContents();
            IDocument document = null;
            if (null != source) {
                document = new Document(source);
                int bByteOffset = bOffset;
                int eByteOffset = eOffset;
                int startPosition = 0;
                int contentLength = 0;

                if (bByteOffset >= 0 && eByteOffset > 0) {
                    // process the offset.
                    byte[] byteSource = source.getBytes();
                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    outStream.write(byteSource, 0, bByteOffset);

                    String sourcePrefix = outStream.toString();

                    outStream = new ByteArrayOutputStream();
                    outStream.write(byteSource, bByteOffset, eByteOffset
                            - bByteOffset > 0 ? eByteOffset - bByteOffset : 0);
                    String sourceRoot = outStream.toString();

                    int bCharOffset = sourcePrefix.length();
                    int eCharOffset = bCharOffset + sourceRoot.length();
                    int charLength = eCharOffset - bCharOffset;

                    startPosition = bCharOffset;
                    contentLength = charLength;
                } else {
                    startPosition = 0;
                    contentLength = source.length();
                }

                final CodeFormatter codeFormatter = ToolFactory
                        .createCodeFormatter(options);

                final TextEdit textEdit = codeFormatter.format(
                        CodeFormatter.K_COMPILATION_UNIT
                                | CodeFormatter.F_INCLUDE_COMMENTS, source,
                        startPosition, contentLength, 0,
                        EclipseJdtUtil.getLineDelimiter(src));

                try {
                    textEdit.apply(document);
                } catch (MalformedTreeException e) {
                    LOGGER.error("", e);
                } catch (BadLocationException e) {
                    LOGGER.error("", e);
                }
            }
            return document;
        } catch (JavaModelException e) {
            LOGGER.error("", e);
        }
        return null;
    }

    /**
     * Saves the source by the supplied document.
     * 
     * @param src the source by compilation unit.
     * @param document the document to save.
     * @return true if save successfully, false otherwise.
     */
    public boolean save(ICompilationUnit src, IDocument document) {
        if (null != src && null != document) {
            try {
                src.getBuffer().setContents(document.get());
                if (src.isWorkingCopy()) {
                    src.commitWorkingCopy(false, null);
                }

                src.save(null, false);
                return true;
            } catch (final JavaModelException e) {
                LOGGER.error("", e);
            }
        }
        return false;
    }

    public void generateElementComment(ICompilationUnit src, int offset)
            throws Exception {
        if (null != src) {
            int offsetLimit = src.getBuffer().getContents().getBytes().length;
            if (offset >= 0 && offset <= offsetLimit) {
                IJavaElement element = src.getElementAt(offset);

                if (null != element)
                    generateElementComment(src, offset, element);
            }
        }
    }

    public void generateElementComment(ICompilationUnit src, int offset,
            IJavaElement element) throws Exception {
        if (null != src && null != element) {
            CompilationUnit cu = ASTUtil.getCompilationUnit(src, true);
            ASTNode node = ASTUtil.findNode(cu, offset, element);
            if (null != node) {
                // performs the comment.
                comment(src, node, element);
            }
            ASTUtil.commitCompilationUnit(src, cu);
        }
    }

    protected void comment(ICompilationUnit src, ASTNode node,
            IJavaElement element) throws Exception {
        Javadoc javadoc = null;
        boolean isNew = false;

        if (node instanceof PackageDeclaration) {
            if (null == javadoc) {
                isNew = true;
                javadoc = node.getAST().newJavadoc();
                ((PackageDeclaration) node).setJavadoc(javadoc);
            }
        } else {
            javadoc = ((BodyDeclaration) node).getJavadoc();
            if (null == javadoc) {
                isNew = true;
                javadoc = node.getAST().newJavadoc();
                ((BodyDeclaration) node).setJavadoc(javadoc);
            }
        }

        switch (node.getNodeType()) {
        // case ASTNode.PACKAGE_DECLARATION:
        // comment the package declaration.
        // break;
            case ASTNode.ENUM_DECLARATION:
            case ASTNode.TYPE_DECLARATION:
                // comment the declaration of the enum or type.
                commentType(src, javadoc, element, isNew);
                break;
            case ASTNode.METHOD_DECLARATION:
                // comment the method declaration.
                commentMethod(src, javadoc, element, isNew);
                break;
            default:
                // comment the other.
                commentOther(src, javadoc, element, isNew);
                break;
        }
    }

    /**
     * Comments a type declaration.
     * 
     * @param src the source.
     * @param javadoc the javadoc instance.
     * @param element the element of java.
     * @param isNew true if there was no previous javadoc for this element.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void commentType(ICompilationUnit src, Javadoc javadoc,
            IJavaElement element, boolean isNew) throws Exception {
        if (element.getParent().getElementType() == IJavaElement.COMPILATION_UNIT) {
            List<TagElement> tags = javadoc.tags();
            if (isNew) {
                addTag(javadoc, tags.size(), null, null);
                addTag(javadoc, tags.size(), null, null);
                addTag(javadoc, tags.size(), TagElement.TAG_AUTHOR, getAuthor());
            } else {
                // check if author tag exists.
                int index = -1;
                for (int i = 0; i < tags.size(); i++) {
                    TagElement tag = tags.get(i);
                    if (TagElement.TAG_AUTHOR.equals(tag.getTagName())) {
                        String authorText = tag.fragments().isEmpty() ? null
                                : ((TextElement) tag.fragments().get(0))
                                        .getText();
                        // don't replace if author tag isn't the same.
                        if (!Strings.isNullOrEmpty(authorText)) {
                            index = -1;
                            break;
                        }

                        index = i + 1;
                    } else if (null != tag.getTagName() && -1 == index) {
                        index = i;
                    } else if (i == tags.size() - 1 && -1 == index) {
                        index = i + 1;
                    }
                }

                // insert author tag if it doesn't exists.
                String author = getAuthor();
                if (index > -1) {
                    TagElement authorTag = javadoc.getAST().newTagElement();
                    TextElement authorText = javadoc.getAST().newTextElement();
                    authorText.setText(author);
                    authorTag.setTagName(TagElement.TAG_AUTHOR);

                    authorTag.fragments().add(authorText);
                    tags.add(index, authorTag);
                }

                // add the version?
            }
        } else {
            commentOther(src, javadoc, element, isNew);
        }
    }

    /**
     * Comments a method declaration.
     * 
     * @param src the source.
     * @param javadoc the javadoc instance.
     * @param element the element of java.
     * @param isNew true if there was no previous javadoc for this element.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void commentMethod(ICompilationUnit src, Javadoc javadoc,
            IJavaElement element, boolean isNew) throws Exception {
        List<TagElement> tags = javadoc.tags();
        IMethod method = (IMethod) element;

        if (isNew) {
            // see if method is overriding / implementing method from
            // superclass.
            IType parentType = null;
            TypeInfo[] types = TypeUtil
                    .getSuperTypes(method.getDeclaringType());
            for (TypeInfo info : types) {
                if (MethodUtil.containsMethod(info, method)) {
                    parentType = info.getType();
                    break;
                }
            }

            // if an inherited method, add inheritDoc and @see
            if (null != parentType && !method.isConstructor()) {
                addTag(javadoc, tags.size(), null, INHERIT_DOC);
            } else {
                addTag(javadoc, tags.size(), null, null);
            }
        }

        // only add/update tags if javadoc doesn't contain inheritDoc.
        boolean update = true;
        for (TagElement tag : tags) {
            if (null == tag.getTagName() && tag.fragments().size() > 0) {
                String text = "";
                Object o = tag.fragments().get(0);
                if (o instanceof TagElement) {
                    text = ((TagElement) o).getTagName();
                } else if (o instanceof TextElement) {
                    text = ((TextElement) o).getText();
                }

                if (INHERIT_DOC.contains(text)) {
                    update = false;
                    break;
                }
            }
        }

        if (update) {
            addUpdateParamTags(javadoc, method, isNew);
            addUpdateReturnTag(javadoc, method, isNew);
            addUpdateThrowsTag(javadoc, method, isNew);
        }
    }

    /**
     * Comments everything else.
     * 
     * @param src the source.
     * @param javadoc the javadoc.
     * @param element the java element instance.
     * @param isNew true if there was no previous javadoc for this element.
     * @throws Exception
     */
    private void commentOther(ICompilationUnit src, Javadoc javadoc,
            IJavaElement element, boolean isNew) throws Exception {
        if (isNew) {
            addTag(javadoc, 0, null, null);
        }
    }

    /**
     * Adds a tag to the supplied list of tags.
     * 
     * @param javadoc the javadoc instance.
     * @param index the index to insert the new tag at.
     * @param name the tag name.
     * @param text the tag text.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected void addTag(Javadoc javadoc, int index, String name, String text)
            throws Exception {
        TagElement tag = javadoc.getAST().newTagElement();
        tag.setTagName(name);

        if (null != text) {
            TextElement textElement = javadoc.getAST().newTextElement();
            textElement.setText(text);

            List<ASTNode> fragments = tag.fragments();
            fragments.add(textElement);
        }

        List<TagElement> tags = javadoc.tags();
        tags.add(tag);
    }

    /**
     * Add or update the param tags for the given method.
     * 
     * @param javadoc the javadoc instance.
     * @param method the method.
     * @param isNew true if we're adding to brand new javadoc.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected void addUpdateParamTags(Javadoc javadoc, IMethod method,
            boolean isNew) throws Exception {
        List<TagElement> tags = javadoc.tags();
        String[] params = method.getParameterNames();
        if (isNew) {
            for (String param : params) {
                addTag(javadoc, tags.size(), TagElement.TAG_PARAM, param);
            }
        } else {
            // find current params.
            int index = 0;
            Map<String, TagElement> current = Maps.newHashMap();
            for (int i = 0; i < tags.size(); i++) {
                TagElement tag = tags.get(i);
                if (TagElement.TAG_PARAM.equals(tag.getTagName())) {
                    if (current.isEmpty()) {
                        index = i;
                    }

                    Object element = tag.fragments().size() > 0 ? tag
                            .fragments().get(0) : null;
                    if (null != element && element instanceof Name) {
                        String name = ((Name) element).getFullyQualifiedName();
                        current.put(name, tag);
                    } else {
                        current.put(String.valueOf(i), tag);
                    }
                } else {
                    if (!current.isEmpty()) {
                        break;
                    }

                    if (null == tag.getTagName())
                        index = i + 1;
                }
            }

            if (!current.isEmpty()) {
                for (int i = 0; i < params.length; i++) {
                    if (current.containsKey(params[i])) {
                        TagElement tag = (TagElement) current.get(params[i]);
                        int currentIndex = tags.indexOf(tag);
                        if (currentIndex != i) {
                            tags.remove(tag);
                            tags.add(index + i, tag);
                        }
                        current.remove(params[i]);
                    } else {
                        addTag(javadoc, index + i, TagElement.TAG_PARAM,
                                params[i]);
                    }
                }

                // remove any other param tags.
                for (TagElement tag : current.values()) {
                    tags.remove(tag);
                }
            } else {
                for (int i = 0; i < params.length; i++) {
                    addTag(javadoc, index + i, TagElement.TAG_PARAM, params[i]);
                }
            }
        }
    }

    /**
     * Add or update the return tag for the given method.
     * 
     * @param javadoc the javadoc instance.
     * @param method the method.
     * @param isNew true if we're adding to brand new javadoc.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected void addUpdateReturnTag(Javadoc javadoc, IMethod method,
            boolean isNew) throws Exception {
        List<TagElement> tags = javadoc.tags();

        // get return type from element.
        if (!method.isConstructor()) {
            String returnType = Signature.getSignatureSimpleName(method
                    .getReturnType());
            if (!"void".equals(returnType)) {
                if (isNew) {
                    addTag(javadoc, tags.size(), TagElement.TAG_RETURN, null);
                } else {
                    // search starting from the bottom since @return should be
                    // near the end.
                    int index = tags.size();
                    for (int i = index - 1; i >= 0; i--) {
                        TagElement tag = tags.get(i);
                        // return tag already exists?
                        if (TagElement.TAG_RETURN.equals(tag.getTagName())) {
                            index = -1;
                            break;
                        }

                        // if we hit the param tags, or the main text, insert
                        // below them.
                        if (TagElement.TAG_PARAM.equals(tag.getTagName())
                                || null == tag.getTagName()) {
                            index = i + 1;
                            break;
                        }
                        index = i;
                    }

                    if (index > -1) {
                        addTag(javadoc, index, TagElement.TAG_RETURN, null);
                    }
                }
            } else {
                // remove any return tag that may exists.
                for (int i = tags.size() - 1; i >= 0; i--) {
                    TagElement tag = tags.get(i);
                    // return tag already exists?
                    if (TagElement.TAG_RETURN.equals(tag.getTagName())) {
                        tags.remove(tag);
                    }
                    // if we hit the param tags, or the main text we can stop.
                    if (TagElement.TAG_PARAM.equals(tag.getTagName())
                            || null == tag.getTagName()) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Add or update the throws tags for the given method.
     * 
     * @param javadoc the javadoc instance.
     * @param method the method to do.
     * @param isNew true if we're adding to brand new javadoc.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected void addUpdateThrowsTag(Javadoc javadoc, IMethod method,
            boolean isNew) throws Exception {
        List<TagElement> tags = javadoc.tags();

        // get throws exceptions from elements.
        String[] exceptionTypes = method.getExceptionTypes();
        if (isNew && exceptionTypes.length > 0) {
            addTag(javadoc, tags.size(), null, null);
            for (int i = 0; i < exceptionTypes.length; i++) {
                addTag(javadoc, tags.size(), TagElement.TAG_THROWS,
                        Signature.getSignatureSimpleName(exceptionTypes[i]));
            }
        } else {
            // get current throws tags.
            Map<String, TagElement> current = Maps.newHashMap();
            int index = tags.size();
            for (int i = index - 1; i >= 0; i--) {
                TagElement tag = tags.get(i);
                if (TagElement.TAG_THROWS.equals(tag.getTagName())) {
                    index = index == tags.size() ? i + 1 : index;
                    Name name = tag.fragments().isEmpty() ? null : (Name) tag
                            .fragments().get(0);
                    if (null != name) {
                        String text = name.getFullyQualifiedName();
                        String key = THROWS_PATTERN.matcher(text).replaceFirst(
                                "$1");
                        current.put(key, tag);
                    } else {
                        current.put(String.valueOf(i), tag);
                    }
                }

                // if we hit the return tag, a param tag, or the main text we
                // can stop.
                if (TagElement.TAG_PARAM.equals(tag.getTagName())
                        || TagElement.TAG_RETURN.equals(tag.getTagName())
                        || null == tag.getTagName()) {
                    break;
                }
            }

            // see what needs to be added / removed.
            for (int i = 0; i < exceptionTypes.length; i++) {
                String name = Signature
                        .getSignatureSimpleName(exceptionTypes[i]);
                if (!current.containsKey(name)) {
                    addTag(javadoc, index, TagElement.TAG_THROWS, name);
                } else {
                    current.remove(name);
                }
            }

            // remove any left over throws clauses.
            for (TagElement tag : current.values()) {
                tags.remove(tag);
            }
        }
    }

    private String getAuthor() throws Exception {
        return System.getProperty("user.name", "");
    }

    /**
     * @param src
     */
    public Object organizeImports(ICompilationUnit src, int offset)
            throws Exception {
        int oldLength = src.getBuffer().getLength();
        if (oldLength == 0 || offset <= 0 || offset > oldLength) {
            return null;
        }

        CompilationUnit astRoot = SharedASTProvider.getAST(src,
                SharedASTProvider.WAIT_YES, null);

        ChooseImports query = new ChooseImports(src.getJavaProject()
                .getProject(), new String[] {});

        CodeGenerationSettings settings = JavaPreferencesSettings
                .getCodeGenerationSettings(src.getJavaProject());

        OrganizeImportsOperation op = new OrganizeImportsOperation(src,
                astRoot, settings.importIgnoreLowercase, true /* save */, true,
                query);

        TextEdit edit = op.createTextEdit(null);

        if (null != query.choices && !query.choices.isEmpty()) {
            return query.choices;
        }

        if (null != edit) {
            JavaModelUtil.applyEdit(src, edit, true, null);
            if (src.isWorkingCopy()) {
                src.commitWorkingCopy(false, null);
            }
        }

        // our own support for grouping imports based on package levels.
        TextEdit groupingEdit = importGroupingEdit(src);
        if (null != groupingEdit) {
            if (null == edit)
                edit = groupingEdit;
            JavaModelUtil.applyEdit(src, groupingEdit, true, null);
            if (src.isWorkingCopy())
                src.commitWorkingCopy(false, null);
        }

        if (null != edit) {
            if (edit.getOffset() < offset) {
                offset += src.getBuffer().getLength() - oldLength;
            }
            
            return /* Position */null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private TextEdit importGroupingEdit(ICompilationUnit src) throws Exception {
        int separationLevel = 0;
        CompilationUnit astRoot = SharedASTProvider.getAST(src,
                SharedASTProvider.WAIT_YES, null);

        List<ImportDeclaration> imports = astRoot.imports();
        String lineDelim = src.findRecommendedLineSeparator();
        MultiTextEdit edit = new MultiTextEdit();
        ImportDeclaration next = null;

        for (int i = imports.size() - 1; i >= 0; i--) {
            ImportDeclaration imprt = imports.get(i);
            int end = imprt.getStartPosition() + imprt.getLength()
                    + lineDelim.length();
            if (null != next
                    && end == next.getStartPosition()
                    && !ImportUtil.importsInSameGroup(separationLevel, imprt,
                            next)) {
                edit.addChild(new InsertEdit(end, lineDelim));
            }
            next = imprt;
        }

        return edit.getChildrenSize() > 0 ? edit : null;
    }

    private class ChooseImports implements
            OrganizeImportsOperation.IChooseImportQuery {

        private List<List<String>> choices;
        private IProject project;
        private Set<String> types;

        /**
         * Creates an new ChooseImports instance.
         * 
         * @param project
         * @param types
         */
        public ChooseImports(IProject project, String[] types) {
            this.project = project;
            if (null != types) {
                this.types = Sets.newHashSet(types);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TypeNameMatch[] chooseImports(TypeNameMatch[][] choices,
                ISourceRange[] range) {
            List<TypeNameMatch> chosen = Lists.newArrayList();
            this.choices = Lists.newArrayList();

            try {
                for (TypeNameMatch[] matches : choices) {
                    boolean foundChoice = false;
                    if (null != types && !types.isEmpty()) {
                        for (TypeNameMatch match : matches) {
                            if (types.contains(match.getFullyQualifiedName())) {
                                foundChoice = true;
                                chosen.add(match);
                                break;
                            }
                        }
                    }

                    if (!foundChoice) {
                        List<String> names = Lists
                                .newArrayListWithCapacity(matches.length);

                        for (TypeNameMatch match : matches) {
                            String name = match.getFullyQualifiedName();
                            if (!ImportUtil.isImportExcluded(project, name)) {
                                names.add(name);
                            }
                        }

                        if (names.size() == 1) {
                            for (TypeNameMatch match : matches) {
                                if (names.get(0).equals(
                                        match.getFullyQualifiedName())) {
                                    chosen.add(match);
                                    break;
                                }
                            }
                        } else if (!names.isEmpty()) {
                            Collections.sort(names);
                            this.choices.add(names);
                        }
                    }
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

            return chosen.toArray(new TypeNameMatch[chosen.size()]);
        }
    }
}
