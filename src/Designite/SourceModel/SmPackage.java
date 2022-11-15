package Designite.SourceModel;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;

import Designite.InputArgs;
import Designite.metrics.TypeMetricsJava;
import Designite.smells.models.DesignCodeSmell;
import Designite.utils.CSVUtils;
import Designite.utils.Constants;
import Designite.utils.models.Edge;

public class SmPackage extends SmSourceItem {
	private List<CompilationUnit> compilationUnitList;
	private List<SmType> typeList = new ArrayList<>();
	private SmProject parentProject;
	private Map<SmType, TypeMetricsJava> metricsMapping = new HashMap<>();
	private Map<SmType, List<DesignCodeSmell>> smellMapping = new HashMap<>();
	private InputArgs inputArgs;

	public SmPackage(String packageName, SmProject parentObj, InputArgs inputArgs) {
		name = packageName;
		compilationUnitList = new ArrayList<CompilationUnit>();
		parentProject = parentObj;
		this.inputArgs = inputArgs;
	}

	public SmProject getParentProject() {
		return parentProject;
	}

	
	 public List<CompilationUnit> getCompilationUnitList() { 
		 return compilationUnitList; 
     }
	 

	public List<SmType> getTypeList() {
		return typeList;
	}

	void addCompilationUnit(CompilationUnit unit) {
		compilationUnitList.add(unit);
	}

	private void addNestedClass(List<SmType> list) {
		if (list.size() > 1) {
			for (int i = 1; i < list.size(); i++) {
				typeList.add(list.get(i));
				list.get(0).addNestedClass(list.get(i));
				list.get(i).setNestedClass(list.get(0).getTypeDeclaration());
			}
		}
	}

	private void parseTypes(SmPackage parentPkg) {
		for (SmType type : typeList) {
			type.parse();
		}
	}

	@Override
	public void printDebugLog(PrintWriter writer) {
		print(writer, "Package: " + name);
		for (SmType type : typeList) {
			type.printDebugLog(writer);
		}
		print(writer, "----");
	}
	
	@Override
	public void parse() {

		for (CompilationUnit unit : compilationUnitList) {

			TypeVisitor visitor = new TypeVisitor(unit, this, inputArgs);
			unit.accept(visitor);
			List<SmType> list = visitor.getTypeList();
			if (list.size() > 0) {
				if (list.size() == 1) {
					typeList.addAll(list);
				} else {
					typeList.add(list.get(0));
					addNestedClass(list);
				}
			}
		}
		parseTypes(this);
	}

	@Override
	public void resolve() {
		for (SmType type : typeList) {
			type.resolve();
		}
	}

	public void extractTypeMetrics() {
		for (SmType type : typeList) {
			type.extractMethodMetrics();
			TypeMetricsJava metrics = new TypeMetricsJava(type);
			metrics.extractMetrics();
			metricsMapping.put(type, metrics);
			exportMetricsToCSV(metrics, type.getName());
			updateDependencyGraph(type);
		}
	}
	
	private void updateDependencyGraph(SmType type) {
		if (type.getReferencedTypeList().size() > 0) {
			for (SmType dependency : type.getReferencedTypeList()) {
				getParentProject().getHierarchyGraph().addEdge(new Edge(type, dependency));
			}
		}
		getParentProject().getHierarchyGraph().addVertex(type);
	}
	
	private void exportMetricsToCSV(TypeMetricsJava metrics, String typeName) {
		String path = inputArgs.getOutputFolder()
				+ File.separator + Constants.TYPE_METRICS_PATH_SUFFIX;
		CSVUtils.addToCSVFile(path, getMetricsAsARow(metrics, typeName));
	}
	
	private String getMetricsAsARow(TypeMetricsJava metrics, String typeName) {
		return getParentProject().getName()
				+ "," + getName()
				+ "," + typeName
				+ "," + metrics.getNumOfFields()
				+ "," + metrics.getNumOfPublicFields()
				+ "," + metrics.getNumOfMethods()
				+ "," + metrics.getNumOfPublicMethods()
				+ "," + metrics.getNumOfLines()
				+ "," + metrics.getWeightedMethodsPerClass()
				+ "," + metrics.getNumOfChildren()
				+ "," + metrics.getInheritanceDepth()
				+ "," + metrics.getLcom()
				+ "," + metrics.getNumOfFanInTypes()
				+ "," + metrics.getNumOfFanOutTypes()
				+ "\n";
	}

}
