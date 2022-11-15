package Designite.SourceModel;

import java.io.PrintWriter;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class SmLocalVar extends SmEntitiesWithType {
	private VariableDeclarationFragment localVarFragment;
	private SmMethod parentMethod;
	private VariableDeclarationStatement localVarDecl;

	public SmLocalVar(VariableDeclarationStatement varDecl, VariableDeclarationFragment localVar, SmMethod method) {
		this.localVarFragment = localVar;
		parentMethod = method;
		name = localVarFragment.getName().toString();
		localVarDecl = varDecl;
	}

	
	@Override
	public SmType getParentType() {
		return this.parentMethod.getParentType();
	}

	@Override
	public void printDebugLog(PrintWriter writer) {
		print(writer, "\t\t\tLocalVar: " + getName());
		print(writer, "\t\t\tParent method: " + this.parentMethod.getName());

		if (!isPrimitiveType()) {
			if (typeInfo.isParametrizedType()) {
				print(writer, "\t\t\tParameter types: " + typeInfo.getStringOfNonPrimitiveParameters());
			}
			else {
				print(writer, "\t\t\tVariable type: " + getType());
			}
		}
		else
			print(writer, "\t\t\tPrimitive variable type: " + getPrimitiveType());
		print(writer, "\t\t\t----");
	}

	@Override
	public void resolve() {
		Resolver resolver = new Resolver();
		typeInfo = resolver.resolveVariableType(localVarDecl.getType(), parentMethod.getParentType().getParentPkg().getParentProject(), getParentType());
	}
	
	@Override
	public String toString() {
		return "Local variable name=" + name
		+ ", type=" + localVarDecl.getType()
		+ ", isParameterizedType=" + localVarDecl.getType().isParameterizedType();
	}
	
}
