package Designite.SourceModel;

import java.io.PrintWriter;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class SmParameter extends SmEntitiesWithType {
	private SmMethod parentMethod;
	private SingleVariableDeclaration variableDecl;
	
	public SmParameter(SingleVariableDeclaration variable, SmMethod methodObj) {
		name = variable.getName().toString();
		this.parentMethod = methodObj;
		variableDecl = variable;
		
	}

	
	public SmMethod getParent() {
		return parentMethod;
	}
	
	@Override
	public SmType getParentType() {
		return this.parentMethod.getParentType();
	}
	
	@Override
	public void printDebugLog(PrintWriter writer) {
		print(writer, "\t\t\tParameter: " + name);
		print(writer, "\t\t\tParent Method: " + getParent().getName());
		if (!isPrimitiveType() && getType() != null)
			print(writer, "\t\t\tParameter type: " + getType().getName());
		else {
				print(writer, "\t\t\tPrimitive parameter type: " + getPrimitiveType());
		}
		print(writer, "\t\t\t----");
	}

	@Override
	public void resolve() {
		Resolver resolver = new Resolver();
		typeInfo = resolver.resolveVariableType(variableDecl.getType(), parentMethod.getParentType().getParentPkg().getParentProject(), getParentType());
	}

	public Type getTypeBinding() {
		return variableDecl.getType();
	}
	
	@Override
	public String toString() {
		return "Parameter=" + name
				+ ", type=" + getTypeBinding()
				+ ", is=" + getTypeBinding().getNodeType();
	}
	
	@Override
	public void parse() {
		
	}
	
}
