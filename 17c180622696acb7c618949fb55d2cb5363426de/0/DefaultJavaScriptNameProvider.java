package org.stjs.generator.name;

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import org.stjs.generator.GenerationContext;
import org.stjs.generator.GeneratorConfiguration;
import org.stjs.generator.javac.ElementUtils;
import org.stjs.generator.javac.InternalUtils;
import org.stjs.generator.utils.ClassUtils;
import org.stjs.generator.utils.JavaNodes;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;

/**
 * This class implements the naming strategy transforming Java element names in JavaScript names.
 *
 * @author acraciun
 */
public class DefaultJavaScriptNameProvider implements JavaScriptNameProvider {
	private static final String JAVA_LANG_PACKAGE = "java.lang.";
	private static final int JAVA_LANG_LENGTH = JAVA_LANG_PACKAGE.length();

	private final Map<String, DependencyType> resolvedRootTypes = new HashMap<String, DependencyType>();
	private final Map<TypeMirror, TypeInfo> resolvedTypes = new HashMap<TypeMirror, TypeInfo>();

	private class TypeInfo {
		private final String fullName;
		private final Element rootTypeElement;

		public TypeInfo(String fullName, Element rootTypeElement) {
			this.fullName = fullName;
			this.rootTypeElement = rootTypeElement;
		}

		public String getFullName() {
			return fullName;
		}

		public Element getRootTypeElement() {
			return rootTypeElement;
		}

	}

	private String addNameSpace(Element rootTypeElement, GenerationContext<?> context, String name) {
		String namespace = context.wrap(rootTypeElement).getNamespace();
		if (namespace.isEmpty()) {
			return name;
		}
		return namespace + "." + name;
	}

	@Override
	public String getTypeName(GenerationContext<?> context, TypeMirror type, DependencyType dependencyType) {
		TypeInfo typeInfo = resolvedTypes.get(type);
		if (typeInfo != null) {
			// make sure we have the strictest dep type
			addResolvedType(typeInfo.getRootTypeElement(), dependencyType);
			return typeInfo.getFullName();
		}

		if (type instanceof DeclaredType) {
			DeclaredType declaredType = (DeclaredType) type;
			String name = InternalUtils.getSimpleName(declaredType.asElement());
			Element rootTypeElement = declaredType.asElement();
			for (DeclaredType enclosingType = JavaNodes.getEnclosingType(declaredType); enclosingType != null; enclosingType = JavaNodes
					.getEnclosingType(enclosingType)) {
				rootTypeElement = enclosingType.asElement();
				name = InternalUtils.getSimpleName(rootTypeElement) + "." + name;
			}

			checkAllowedType(rootTypeElement, context);
			addResolvedType(rootTypeElement, dependencyType);

			String fullName = addNameSpace(rootTypeElement, context, name);
			resolvedTypes.put(type, new TypeInfo(fullName, rootTypeElement));
			return fullName;
		}
		if (type instanceof ArrayType) {
		    ArrayType atype = (ArrayType) type;
		    TypeMirror componentType = atype.getComponentType();
		    TypeKind kind = componentType.getKind();
		    switch (kind) {
            case BOOLEAN:
                return "Int8Array";
            case BYTE:
                return "Int8Array";
            case SHORT:
                return "Int16Array";
            case CHAR:
                return "Uint16Array";
            case INT:
                return "Int32Array";
            case FLOAT:
                return "Float32Array";
            case DOUBLE:
                return "Float64Array";
            default:
                return "Array";
            }
		}
		if (type instanceof WildcardType) {
			// ? extends Type1 super Type2
			// XXX what to return here !?
			return "Object";
		}
		return type.toString();
	}

	private void typeNotAllowedException(GenerationContext<?> context, String name) {
		context.addError(context.getCurrentPath().getLeaf(), "The usage of the class " + name
				+ " is not allowed. If it's one of your own bridge types, "
				+ "please add the annotation @STJSBridge to the class or to its package.");
	}

	private boolean isJavaLangClassAllowed(GenerationContext<?> context, String name) {
		GeneratorConfiguration configuration = context.getConfiguration();
		if (name.startsWith(JAVA_LANG_PACKAGE) && configuration.getAllowedJavaLangClasses().contains(name.substring(JAVA_LANG_LENGTH))) {
			return true;
		}

		return false;
	}

	private boolean isPackageAllowed(GenerationContext<?> context, String name) {
		if (name.startsWith(JAVA_LANG_PACKAGE)) {
			return false;
		}
		GeneratorConfiguration configuration = context.getConfiguration();
		for (String packageName : configuration.getAllowedPackages()) {
			if (name.startsWith(packageName)) {
				return true;
			}
		}
		return false;
	}

	private boolean isBridge(GenerationContext<?> context, String name) {
		if (name.startsWith(JAVA_LANG_PACKAGE)) {
			return false;
		}

		return ClassUtils.isBridge(context.getBuiltProjectClassLoader(), ClassUtils.getClazz(context.getBuiltProjectClassLoader(), name));
	}

	private void checkAllowedType(Element rootTypeElement, GenerationContext<?> context) {
		String name = ElementUtils.getQualifiedClassName(rootTypeElement).toString();
		if (name.isEmpty()) {
			return;
		}
		if (isJavaLangClassAllowed(context, name)) {
			return;
		}

		if (isImportedStjsClass(context, name)) {
			return;
		}

		if (isPackageAllowed(context, name)) {
			return;
		}

		// ClassUtils.isBridge accepts all java.lang classes, that are actually not allowed
		if (isBridge(context, name)) {
			return;
		}

		typeNotAllowedException(context, name);
	}

	private boolean isImportedStjsClass(GenerationContext<?> context, String className) {
		String stjsPropertiesName = ClassUtils.getPropertiesFileName(className);
		return context.getBuiltProjectClassLoader().getResource(stjsPropertiesName) != null;
	}

	private void addResolvedType(Element rootTypeElement, DependencyType depType) {
		String name = ElementUtils.getQualifiedClassName(rootTypeElement).toString();
		if (!name.startsWith("java.lang.")) {
			DependencyType prevDepType = resolvedRootTypes.get(name);
			if (prevDepType == null || depType.isStricter(prevDepType)) {
				resolvedRootTypes.put(name, depType);
			}
		}
	}

	@Override
	public String getVariableName(GenerationContext<?> context, IdentifierTree treeNode, TreePath path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMethodName(GenerationContext<?> context, MethodTree tree, TreePath path) {
		return tree.getName().toString();
	}

	@Override
	public String getMethodName(GenerationContext<?> context, MethodInvocationTree tree, TreePath path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTypeName(GenerationContext<?> context, Element type, DependencyType dependencyType) {
		if (type == null) {
			return null;
		}
		return getTypeName(context, type.asType(), dependencyType);
	}

	@Override
	public Map<String, DependencyType> getResolvedTypes() {
		return resolvedRootTypes;
	}

}