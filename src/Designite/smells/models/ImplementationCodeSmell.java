package Designite.smells.models;

public class ImplementationCodeSmell extends CodeSmell {

	private String typeName;
	private String methodName;
	private String smellName;

	public ImplementationCodeSmell(String projectName
			, String packageName
			, String typeName
			, String methodName
			, String smellName) {
		super(projectName, packageName);
		this.typeName = typeName;
		this.methodName = methodName;
		this.smellName = smellName;
	}

	@Override
	public String toString() {
		return getProjectName()
				+ "," + getPackageName()
				+ "," + typeName
				+ "," + methodName
				+ "," + smellName
				+ "\n";
	}

}
