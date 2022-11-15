package Designite.SourceModel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class FieldVisitor extends ASTVisitor {
	List<SmField> fields = new ArrayList<SmField>();
	private SmType parentType;

	public FieldVisitor(SmType parentType) {
		super();
		this.parentType = parentType;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		List<VariableDeclarationFragment> fieldList = fieldDeclaration.fragments();
		for (VariableDeclarationFragment field : fieldList) {
			SmField newField = new SmField(fieldDeclaration, field, parentType);
			fields.add(newField);
		}

		return super.visit(fieldDeclaration);
	}

	public List<SmField> getFields() {
		return fields;
	}

}
