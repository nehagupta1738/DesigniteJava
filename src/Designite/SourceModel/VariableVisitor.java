package Designite.SourceModel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public class VariableVisitor extends ASTVisitor {
	List<SmParameter> parameters = new ArrayList<SmParameter>();
	private SmMethod parentMethod;
	
	public VariableVisitor(SmMethod methodObj) {
		super();
		this.parentMethod = methodObj;
	}

	@Override
	public boolean visit(SingleVariableDeclaration variable) {
		SmParameter newParameter = new SmParameter(variable, parentMethod);
		parameters.add(newParameter);

		return super.visit(variable);
	}

	public List<SmParameter> getParameterList() {
		return parameters;
	}

}
