package Designite.SourceModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class LocalVarVisitor extends ASTVisitor {
	List<SmLocalVar> localVariables = new ArrayList<SmLocalVar>();
	private SmMethod parentMethod;
	
	public LocalVarVisitor(SmMethod methodObj) {
		this.parentMethod = methodObj;
	}

	public boolean visit(VariableDeclarationStatement variable){
		for (Iterator iter = variable.fragments().iterator(); iter.hasNext();) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();

			SmLocalVar newLocalVar = new SmLocalVar(variable, fragment, parentMethod);
			localVariables.add(newLocalVar);
		}
		return super.visit(variable);
	}
	
	public List<SmLocalVar> getLocalVarList() {
		return localVariables;
	}
	
}
