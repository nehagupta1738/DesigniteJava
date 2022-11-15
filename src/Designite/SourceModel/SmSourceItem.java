package Designite.SourceModel;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;

public abstract class SmSourceItem {
	protected String name;
	protected AccessStates accessModifier;

	public abstract void printDebugLog(PrintWriter writer);
	public abstract void parse();
	public abstract void resolve();

	public String getName() {
		return name;
	}

	public AccessStates getAccessModifier() {
		return accessModifier;
	}

	void setAccessModifier(int modifier) {
		if (Modifier.isPublic(modifier))
			accessModifier = AccessStates.PUBLIC;
		else if (Modifier.isProtected(modifier))
			accessModifier = AccessStates.PROTECTED;
		else if (Modifier.isPrivate(modifier))
			accessModifier = AccessStates.PRIVATE;
		else
			accessModifier = AccessStates.DEFAULT;
	}

	List<SmType> getTypesOfProject(SmProject project) {
		List<SmPackage> pkgList = new ArrayList<>();
		List<SmType> typeList = new ArrayList<>();

		pkgList.addAll(project.getPackageList());
		for (SmPackage pkg : pkgList)
			typeList.addAll(pkg.getTypeList());

		return typeList;
	}


	void print(PrintWriter writer, String str) {
		if (writer != null)
		{
			writer.println(str);
			writer.flush();
		}
		else
			System.out.println(str);
	}

}
