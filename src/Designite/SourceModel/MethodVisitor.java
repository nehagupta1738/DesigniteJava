package Designite.SourceModel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class MethodVisitor extends ASTVisitor {
	List<SmMethod> methods = new ArrayList<SmMethod>();
	private SmType parentType;
	
	public MethodVisitor(TypeDeclaration typeDeclaration, SmType typeObj) {
		super();

		this.parentType = typeObj;
	}
	
	@Override
	public boolean visit(MethodDeclaration method) {
		SmMethod newMethod = new SmMethod(method, parentType);
		methods.add(newMethod);
		
		return super.visit(method);
	}
	
	public List<SmMethod> getMethods(){
		return methods;
	}

}
