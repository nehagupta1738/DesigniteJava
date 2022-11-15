package Designite.SourceModel;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import Designite.InputArgs;

import java.util.ArrayList;
import java.util.List;

public class TypeVisitor extends ASTVisitor{
	private List<SmType> types = new ArrayList<SmType>();
	private List<TypeDeclaration> typeDeclarationList = new ArrayList<TypeDeclaration>();
	private CompilationUnit compilationUnit;
	private SmType newType;
	private SmPackage pkgObj;
	private InputArgs inputArgs;
	
	public TypeVisitor(CompilationUnit cu, SmPackage pkgObj, InputArgs inputArgs) {
		super();
		this.compilationUnit = cu;
		this.pkgObj = pkgObj;
		this.inputArgs = inputArgs;
	}
	
	@Override
	public boolean visit(TypeDeclaration typeDeclaration){
		typeDeclarationList.add(typeDeclaration);
		newType = new SmType(typeDeclaration, compilationUnit, pkgObj, inputArgs);
		types.add(newType);
		
		return super.visit(typeDeclaration);
	}
	
	public SmType getType() {
		return newType;
	}
	
	public List<SmType> getTypeList() {
		return types;
	}

}
