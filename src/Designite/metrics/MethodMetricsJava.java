package Designite.metrics;
import Designite.SourceModel.SmMethod;
import Designite.visitors.MethodControlFlowVisitor;

public class MethodMetricsJava implements MetricExtractor {

	private int numOfParameters;
	private int cyclomaticComplexity;
	private int numOfLines;
	
	private SmMethod method;
	
	public MethodMetricsJava(SmMethod method) {
		this.method = method;
	}
	
	@Override
	public void extractMetrics() {
		extractNumOfParametersMetrics();
		extractCyclomaticComplexity();
		extractNumberOfLines();
	}
	
	private void extractNumOfParametersMetrics() {
		numOfParameters = method.getParameterList().size();
	}
	
	private void extractCyclomaticComplexity() {
		cyclomaticComplexity = calculateCyclomaticComplexity();
	}
	
	private int calculateCyclomaticComplexity() {
		MethodControlFlowVisitor visitor = new MethodControlFlowVisitor();
		method.getMethodDeclaration().accept(visitor);
		return visitor.getNumOfIfStatements()
			 + visitor.getNumOfSwitchCaseStatementsWitoutDefault()
			 + visitor.getNumOfForStatements()
			 + visitor.getNumOfWhileStatements()
		     + visitor.getNumOfDoStatements()
			 + visitor.getNumOfForeachStatements()
			 + 1;
	}
	
	private void extractNumberOfLines() {
		if (methodHasBody()) {
			String body = method.getMethodDeclaration().getBody().toString();
			numOfLines = body.length() - body.replace("\n", "").length();
		}
	}
	
	private boolean methodHasBody() {
		return method.getMethodDeclaration().getBody() != null;
	}
	
	public int getNumOfParameters() {
		return numOfParameters;
	}

	public int getCyclomaticComplexity() {
		return cyclomaticComplexity;
	}

	public int getNumOfLines() {
		return numOfLines;
	}
	
	public SmMethod getMethod() {
		return method;
	}

}
