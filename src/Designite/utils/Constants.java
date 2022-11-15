package Designite.utils;

public class Constants {

	public static final String TYPE_METRICS_PATH_SUFFIX = "typeMetrics.csv";
	public static final String METHOD_METRICS_PATH_SUFFIX = "methodMetrics.csv";

	public static final String TYPE_METRICS_HEADER = "Project Name"
			+ ",Package Name"
			+ ",Type Name"
			+ ",NOF"
			+ ",NOPF"
			+ ",NOM"
			+ ",NOPM"
			+ ",LOC"
			+ ",WMC"
			+ ",NC"
			+ ",DIT"
			+ ",LCOM"
			+ ",FANIN"
			+ ",FANOUT"
			+ "\n";
	
	public static final String METHOD_METRICS_HEADER = "Project Name"
			+ ",Package Name"
			+ ",Type Name"
			+ ",MethodName"
			+ ",LOC"
			+ ",CC"
			+ ",PC"
			+ "\n";

	public static final boolean DEBUG = false;
}
