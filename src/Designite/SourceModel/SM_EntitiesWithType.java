package Designite.SourceModel;

import java.util.List;

public abstract class SM_EntitiesWithType extends SM_SourceItem {
	
	protected TypeInfo typeInfo;

	public boolean isPrimitiveType() {
		return typeInfo.isPrimitiveType();
	}
	
	public SM_Type getParentType() {

		return null;
	}

	public SM_Type getType() {
		return typeInfo.getTypeObj();
	}	
	
	public String getPrimitiveType() {
		return typeInfo.getObjPrimitiveType();
	}
	
	public boolean isParametrizedType() {
		return typeInfo.isParametrizedType();
	}

	
	@Override
	public void parse() {
		
	}
}
