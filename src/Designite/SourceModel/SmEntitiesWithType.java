package Designite.SourceModel;

public abstract class SmEntitiesWithType extends SmSourceItem {
	
	protected TypeInfo typeInfo;

	public boolean isPrimitiveType() {
		return typeInfo.isPrimitiveType();
	}
	
	public SmType getParentType() {

		return null;
	}

	public SmType getType() {
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
