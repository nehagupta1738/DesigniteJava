package Designite.SourceModel;

import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import Designite.utils.models.Vertex;
import Designite.visitors.DirectAceessFieldVisitor;
import Designite.visitors.InstanceOfVisitor;
import Designite.visitors.ThrowVisitor;

public class SmMethod extends SmSourceItem implements Vertex {
		
	private boolean abstractMethod;
	private boolean finalMethod;
	private boolean staticMethod;
	private boolean isConstructor;
	private boolean throwsException;
	private SmType parentType;

	private MethodDeclaration methodDeclaration;

	private List<SmMethod> calledMethodsList = new ArrayList<SmMethod>();
	private List<SmParameter> parameterList = new ArrayList<SmParameter>();
	private List<SmLocalVar> localVarList = new ArrayList<SmLocalVar>();
	private List<MethodInvocation> calledMethods = new ArrayList<MethodInvocation>();
	private List<SmType> referencedTypeList = new ArrayList<SmType>();
	private List<SimpleName> namesInMethod = new ArrayList<>();
	private List<FieldAccess> thisAccessesInMethod = new ArrayList<>();
	private List<SmField> directFieldAccesses = new ArrayList<>();
	private List<Type> typesInInstanceOf = new ArrayList<>();
	private List<SmType> smTypesInInstanceOf = new ArrayList<>();

	public SmMethod(MethodDeclaration methodDeclaration, SmType typeObj) {
		name = methodDeclaration.getName().toString();
		this.parentType = typeObj;
		this.methodDeclaration = methodDeclaration;
		setMethodInfo(methodDeclaration);
		setAccessModifier(methodDeclaration.getModifiers());
	}

	public void setMethodInfo(MethodDeclaration method) {
		int modifiers = method.getModifiers();
		if (Modifier.isAbstract(modifiers))
			abstractMethod = true;
		if (Modifier.isFinal(modifiers))
			finalMethod = true;
		if (Modifier.isStatic(modifiers))
			staticMethod = true;
		if (method.isConstructor())
			isConstructor = true;
	}



	public boolean isStatic() {
		return this.staticMethod;
	}

	public SmType getParentType() {
		return parentType;
	}


	public List<SmParameter> getParameterList() {
		return parameterList;
	}

	public List<SmMethod> getCalledMethods() {
		return calledMethodsList;
	}
	
	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	private void parseParameters() {
		for (SmParameter param : parameterList) {
			param.parse();
		}
	}

	private void parseLocalVar() {
		for (SmLocalVar var : localVarList) {
			var.parse();
		}
	}

	@Override
	public void printDebugLog(PrintWriter writer) {
		print(writer, "\t\tMethod: " + name);
		print(writer, "\t\tParent type: " + this.getParentType().getName());
		print(writer, "\t\tConstructor: " + isConstructor);
		print(writer, "\t\tReturns: " + methodDeclaration.getReturnType2());
		print(writer, "\t\tAccess: " + accessModifier);
		print(writer, "\t\tAbstract: " + abstractMethod);
		print(writer, "\t\tFinal: " + finalMethod);
		print(writer, "\t\tStatic: " + staticMethod);
		print(writer, "\t\tCalled methods: ");
		for(SmMethod method:getCalledMethods())
			print(writer, "\t\t\t" + method.getName());
		for (SmParameter param : parameterList)
			param.printDebugLog(writer);
		for (SmLocalVar var : localVarList)
			var.printDebugLog(writer);
		print(writer, "\t\t----");
	}

	//TODO: Modularize parser with private functions
	@Override
	public void parse() {
		MethodInvVisitor invVisitor = new MethodInvVisitor(methodDeclaration);
		methodDeclaration.accept(invVisitor);
		List<MethodInvocation> invList = invVisitor.getCalledMethods();
		if (invList.size() > 0) {
			calledMethods.addAll(invList);
		}

		List<SingleVariableDeclaration> variableList = methodDeclaration.parameters();
		for (SingleVariableDeclaration var : variableList) {
			VariableVisitor parameterVisitor = new VariableVisitor(this);
			// methodDeclaration.accept(parameterVisitor);
			var.accept(parameterVisitor);
			List<SmParameter> pList = parameterVisitor.getParameterList();
			if (pList.size() > 0) {
				parameterList.addAll(pList);
			}
			parseParameters();
		}

		LocalVarVisitor localVarVisitor = new LocalVarVisitor(this);
		methodDeclaration.accept(localVarVisitor);
		List<SmLocalVar> lList = localVarVisitor.getLocalVarList();
		if (lList.size() > 0) {
			localVarList.addAll(lList);
		}
		parseLocalVar();
		
		DirectAceessFieldVisitor directAceessFieldVisitor = new DirectAceessFieldVisitor();
		methodDeclaration.accept(directAceessFieldVisitor);
		List<SimpleName> names = directAceessFieldVisitor.getNames();
		List<FieldAccess> thisAccesses = directAceessFieldVisitor.getThisAccesses();
		if (names.size() > 0) {
			namesInMethod.addAll(names);
		}
		if (thisAccesses.size() > 0) {
			thisAccessesInMethod.addAll(thisAccesses);
		}
		
		InstanceOfVisitor instanceOfVisitor = new InstanceOfVisitor();
		methodDeclaration.accept(instanceOfVisitor);
		List<Type> instanceOfTypes = instanceOfVisitor.getTypesInInstanceOf();
		if (instanceOfTypes.size() > 0) {
			typesInInstanceOf.addAll(instanceOfTypes);
		}
		
		ThrowVisitor throwVisithor = new ThrowVisitor();
		methodDeclaration.accept(throwVisithor);
		throwsException = throwVisithor.throwsException();
	}

	@Override
	public void resolve() {
		for (SmParameter param : parameterList) {
				param.resolve();
		}
		for (SmLocalVar localVar : localVarList) {
			localVar.resolve();
		}
		calledMethodsList = (new Resolver()).inferCalledMethods(calledMethods, parentType);
		setReferencedTypes();
		setDirectFieldAccesses();
		setSMTypesInInstanceOf();
	}
	
	private void setReferencedTypes() {
		for (SmParameter param : parameterList) {
			if (!param.isPrimitiveType()) {
				addunique(param.getType());
			}
		}
		for (SmLocalVar localVar : localVarList) {
			if (!localVar.isPrimitiveType()) {
				addunique(localVar.getType());
			}
		}
		for (SmMethod methodCall : calledMethodsList) {
			if (methodCall.isStatic()) {
				addunique(methodCall.getParentType());
			}
		}
	}
	
	private void setDirectFieldAccesses() {
		for (FieldAccess thisAccess : thisAccessesInMethod) {
			SmField sameField = getFieldWithSameName(thisAccess.getName().toString());
			if (sameField != null && !directFieldAccesses.contains(sameField)) {
				directFieldAccesses.add(sameField);
			}
		}
		for (SimpleName name : namesInMethod) {
			if (!existsAsNameInLocalVars(name.toString())) {
				SmField sameField = getFieldWithSameName(name.toString());
				if (sameField != null && !directFieldAccesses.contains(sameField)) {
					directFieldAccesses.add(sameField);
				}
			}
		}
	}
	
	private boolean existsAsNameInLocalVars(String name) {
		for (SmLocalVar localVar : localVarList) {
			if (name.equals(localVar.getName())) {
				return true;
			}
		}
		return false;
	}
	
	private SmField getFieldWithSameName(String name) {
		for (SmField field : parentType.getFieldList()) {
			if (name.equals(field.getName())) {
				return field;
			}
		}
		return null;
	}
	
	private void setSMTypesInInstanceOf() {
		Resolver resolver = new Resolver();
		for (Type type : typesInInstanceOf) {
			SmType smType = resolver.resolveType(type, parentType.getParentPkg().getParentProject());
			if (smType != null && !smTypesInInstanceOf.contains(smType)) {
				smTypesInInstanceOf.add(smType);
			}
		}
	}
	
	private void addunique(SmType variableType) {
		if (!referencedTypeList.contains(variableType))
			referencedTypeList.add(variableType);
	}

	public List<SmType> getReferencedTypeList() {
		return referencedTypeList;
	}
	
	public List<SmField> getDirectFieldAccesses() {
		return directFieldAccesses;
	}
	
	public List<SmType> getSMTypesInInstanceOf() {
		return smTypesInInstanceOf;
	}
	
}
