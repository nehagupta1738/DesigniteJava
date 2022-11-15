package Designite.SourceModel;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import Designite.InputArgs;
import Designite.metrics.MethodMetricsJava;
import Designite.smells.models.ImplementationCodeSmell;
import Designite.utils.CSVUtils;
import Designite.utils.Constants;
import Designite.utils.models.Edge;
import Designite.utils.models.Vertex;
import Designite.visitors.StaticFieldAccessVisitor;

//TODO check EnumDeclaration, AnnotationTypeDeclaration and nested classes
public class SmType extends SmSourceItem implements Vertex {
	
	
	private boolean isAbstract = false;
	private boolean isInterface = false;
	private SmPackage parentPkg;

	private TypeDeclaration typeDeclaration;
	private TypeDeclaration containerClass;
	private boolean nestedClass;
	
	private List<SmType> superTypes = new ArrayList<>();
	private List<SmType> subTypes = new ArrayList<>();
	private List<SmType> referencedTypeList = new ArrayList<>();
	private List<SmType> typesThatReferenceThisList = new ArrayList<>();
	private List<SmType> nestedTypesList = new ArrayList<>();
	private List<ImportDeclaration> importList = new ArrayList<>();
	private List<SmMethod> methodList = new ArrayList<>();
	private List<SmField> fieldList = new ArrayList<>();
	private List<Name> staticFieldAccesses = new ArrayList<>();
	private List<SmType> staticFieldAccessList = new ArrayList<>();
	private List<SmType> staticMethodInvocations = new ArrayList<>();
	private Map<SmMethod, MethodMetricsJava> metricsMapping = new HashMap<>();
	private Map<SmMethod, List<ImplementationCodeSmell>> smellMapping = new HashMap<>();
	private InputArgs inputArgs;

	public SmType(TypeDeclaration typeDeclaration, CompilationUnit compilationUnit, SmPackage pkg, InputArgs inputArgs) {
		parentPkg = pkg;
		if (typeDeclaration == null || compilationUnit == null)
			throw new NullPointerException();

		name = typeDeclaration.getName().toString();
		this.typeDeclaration = typeDeclaration;
		this.inputArgs = inputArgs;
		setTypeInfo();
		setAccessModifier(typeDeclaration.getModifiers());
		setImportList(compilationUnit);
	}
	
	public List<SmType> getSuperTypes() {
		return superTypes;
	}
	
	public List<SmType> getSubTypes() {
		return subTypes;
	}
	
	public List<SmType> getReferencedTypeList() {
		return referencedTypeList;
	}
	
	public List<SmType> getTypesThatReferenceThis() {
		return typesThatReferenceThisList;
	}

	public TypeDeclaration getTypeDeclaration() {
		return typeDeclaration;
	}
	
	public void addReferencedTypeList(SmType type) {
		referencedTypeList.add(type);
	}
	
	public void addStaticMethodInvocation(SmType type) {
		if (!this.staticMethodInvocations.contains(type)){
			this.staticMethodInvocations.add(type);
		} 
	}
	
	public void addNestedClass(SmType type) {
		if (!this.nestedTypesList.contains(type)) {
			this.nestedTypesList.add(type);
		}
	}
	
	public SmType getNestedTypeFromName(String typeName) {
		for(SmType nestedType : this.nestedTypesList) {
			if(nestedType.name.equals(typeName)) {
				return nestedType;
			}
		}
		return null;
	}
	
	public List<SmType> getNestedTypes() {
		return this.nestedTypesList;
	}
	
	public boolean containsTypeInReferencedTypeList(SmType type) {
		return referencedTypeList.contains(type);
	}
	
	public void addTypesThatReferenceThisList(SmType type) {
		typesThatReferenceThisList.add(type);
	}
	
	public boolean containsTypeInTypesThatReferenceThisList(SmType type) {
		return typesThatReferenceThisList.contains(type);
	}

	private void setTypeInfo() {
		int modifier = typeDeclaration.getModifiers();
		if (Modifier.isAbstract(modifier)) {
			isAbstract = true;
		}
		if (typeDeclaration.isInterface()) {
			isInterface = true;
		}
	}

	public boolean isInterface() {
		return isInterface;
	}

	public void setNestedClass(TypeDeclaration referredClass) {
		nestedClass = true;
		this.containerClass = referredClass;
	}

	private void setImportList(CompilationUnit unit) {
		ImportVisitor importVisitor = new ImportVisitor();
		unit.accept(importVisitor);
		List<ImportDeclaration> imports = importVisitor.getImports();
		if (imports.size() > 0)
			importList.addAll(imports);
	}

	public List<ImportDeclaration> getImportList() {
		return importList;
	}
	
	private void setSuperTypes() {
		setSuperClass();
		setSuperInterface();
	}
	
	private void setSuperClass() {
		Type superclass = typeDeclaration.getSuperclassType();
		if (superclass != null)
		{
			SmType inferredType = (new Resolver()).resolveType(superclass, parentPkg.getParentProject());
			if(inferredType != null) {
				superTypes.add(inferredType);
				inferredType.addThisAsChildToSuperType(this);
			}
		}
			
	}
	
	private void setSuperInterface() {
		List<Type> superInterfaces = typeDeclaration.superInterfaceTypes();
		if (superInterfaces != null)
		{
			for (Type superInterface : superInterfaces)  {
				SmType inferredType = (new Resolver()).resolveType(superInterface, parentPkg.getParentProject());
				if(inferredType != null) {
					superTypes.add(inferredType);
					inferredType.addThisAsChildToSuperType(this);
				}
			}
		}
			
	}
	
	private void addThisAsChildToSuperType(SmType child) {
		if (!subTypes.contains(child)) {
			subTypes.add(child);
		}
	}

	public List<SmMethod> getMethodList() {
		return methodList;
	}

	public List<SmField> getFieldList() {
		return fieldList;
	}

	public SmPackage getParentPkg() {
		return parentPkg;
	}

	private void parseMethods() {
		for (SmMethod method : methodList) {
			method.parse();
		}
	}

	private void parseFields() {
		for (SmField field : fieldList) {
			field.parse();
		}
	}

	@Override
	public void printDebugLog(PrintWriter writer) {
		print(writer, "\tType: " + name);
		print(writer, "\tPackage: " + this.getParentPkg().getName());
		print(writer, "\tAccess: " + accessModifier);
		print(writer, "\tInterface: " + isInterface);
		print(writer, "\tAbstract: " + isAbstract);
		print(writer, "\tSupertypes: " + ((getSuperTypes().size() != 0) ? getSuperTypes().get(0).getName() : "Object"));
		print(writer, "\tNested class: " + nestedClass);
		if (nestedClass)
			print(writer, "\tContainer class: " + containerClass.getName());
		print(writer, "\tReferenced types: ");
		for (SmType type:referencedTypeList)
			print(writer, "\t\t" + type.getName());
		for (SmField field : fieldList)
			field.printDebugLog(writer);
		for (SmMethod method : methodList)
			method.printDebugLog(writer);
		print(writer, "\t----");
	}


	@Override
	public void parse() {
		MethodVisitor methodVisitor = new MethodVisitor(typeDeclaration, this);
		typeDeclaration.accept(methodVisitor);
		List<SmMethod> mList = methodVisitor.getMethods();
		if (mList.size() > 0)
			methodList.addAll(mList);
		parseMethods();

		FieldVisitor fieldVisitor = new FieldVisitor(this);
		typeDeclaration.accept(fieldVisitor);
		List<SmField> fList = fieldVisitor.getFields();
		if (fList.size() > 0)
			fieldList.addAll(fList);
		parseFields();
		
		StaticFieldAccessVisitor fieldAccessVisitor = new StaticFieldAccessVisitor();
		typeDeclaration.accept(fieldAccessVisitor);
		staticFieldAccesses = fieldAccessVisitor.getStaticFieldAccesses();
	}

	@Override
	public void resolve() {
		for (SmMethod method : methodList)
			method.resolve();
		for (SmField field : fieldList)
			field.resolve();
		setStaticAccessList();
		setReferencedTypes();
		setTypesThatReferenceThis();
		setSuperTypes();
		updateHierarchyGraph();
		updateDependencyGraph();
	}
	
	private void setStaticAccessList() {
		staticFieldAccessList = (new Resolver()).inferStaticAccess(staticFieldAccesses, this);
	}
	
	private void setReferencedTypes() {
		for (SmField field:fieldList)
			if(!field.isPrimitiveType()) {
				addUniqueReference(this, field.getType(), false);
			}	
		for (SmMethod method:methodList) {
			for (SmType refType:method.getReferencedTypeList()) {
				addUniqueReference(this, refType, false);
			}
		}
		for (SmType staticAccessType : staticFieldAccessList) {
			addUniqueReference(this, staticAccessType, false);
		}
		for (SmType methodInvocation : staticMethodInvocations){
			addUniqueReference(this, methodInvocation, false);
			
		}
	}
	
	private void setTypesThatReferenceThis() {
		for (SmType refType : referencedTypeList) {
			addUniqueReference(refType, this, true);
		}
	}
	
	private void updateHierarchyGraph() {
		if (superTypes.size() > 0) {
			for (SmType superType : superTypes) {
				getParentPkg().getParentProject().getHierarchyGraph().addEdge(
						new Edge(this, superType));
			}
		}
		getParentPkg().getParentProject().getHierarchyGraph().addVertex(this);		
	}
	
	private void updateDependencyGraph() {
		if (getReferencedTypeList().size() > 0) {
			for (SmType dependency : getReferencedTypeList()) {
				getParentPkg().getParentProject().getDependencyGraph().addEdge(
						new Edge(this, dependency));
			}
		}
		getParentPkg().getParentProject().getDependencyGraph().addVertex(this);
	}
	
	private void addUniqueReference(SmType type, SmType typeToAdd, boolean invardReference) {
		if(typeToAdd == null)
			return;
		if (invardReference) {
			if (!type.containsTypeInTypesThatReferenceThisList(typeToAdd)) {
				type.addTypesThatReferenceThisList(typeToAdd);//FAN-IN?
			}
		} else {
			if (!type.containsTypeInReferencedTypeList(typeToAdd)) {
				type.addReferencedTypeList(typeToAdd);//FAN-OUT?
			}
		}
	}

	public void extractMethodMetrics() {
		for (SmMethod method : methodList) {
			MethodMetricsJava metrics = new MethodMetricsJava(method);
			metrics.extractMetrics();
			metricsMapping.put(method, metrics);
			exportMethodMetricsToCSV(metrics, method.getName());
		}
	}
	
	public MethodMetricsJava getMetricsFromMethod(SmMethod method) {
		return metricsMapping.get(method);
	}
	
	public void exportMethodMetricsToCSV(MethodMetricsJava metrics, String methodName) {
		String path = inputArgs.getOutputFolder()
				+ File.separator + Constants.METHOD_METRICS_PATH_SUFFIX;
		CSVUtils.addToCSVFile(path, getMetricsAsARow(metrics, methodName));
	}
	
	private String getMetricsAsARow(MethodMetricsJava metrics, String methodName) {
		return getParentPkg().getParentProject().getName()
				+ "," + getParentPkg().getName()
				+ "," + getName()
				+ "," + methodName
				+ "," + metrics.getNumOfLines()
				+ "," + metrics.getCyclomaticComplexity()
				+ "," + metrics.getNumOfParameters()
				+ "\n";
	}

	@Override
	public String toString() {
		return "Type="+name;
	}

}
