package Designite.smells.models;

public class DesignCodeSmell extends CodeSmell {

	private String typeName;
	private String smellName;

	public DesignCodeSmell(String projectName
			, String packageName
			, String typeName
			, String smellName) {
		super(projectName, packageName);
		this.typeName = typeName;
		this.smellName = smellName;
	}

	@Override
	public String toString() {
		return getProjectName()
				+ "," + getPackageName()
				+ "," + typeName
				+ "," + smellName
				+ "\n";
	}

}
