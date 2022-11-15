package Designite;

import java.io.File;

public class InputArgs {
	private String sourceFolder;
	private String outputFolder;
	
	public InputArgs() {
		//It is invoked only in case of error
	}

	public InputArgs(String inputFolderPath, String outputFolderPath) {
		sourceFolder = inputFolderPath;
		outputFolder = outputFolderPath;
		checkEssentialInputs();
	}

	public String getSourceFolder() {
		return sourceFolder;
	}

	public String getOutputFolder() {
		return outputFolder;
	}


	private void checkEssentialInputs() {
		if (sourceFolder==null)
		{
			throw new IllegalArgumentException("Input source folder is not specified.");
		}
		File folder = new File(sourceFolder);
		if (!(folder.exists() && folder.isDirectory()))
		{
			throw new IllegalArgumentException("Input source folder path is not valid.");
		}
		File outFolder = new File(outputFolder);
		if (outFolder.exists() && outFolder.isFile())
		{
			throw new IllegalArgumentException("The specified output folder path is not valid.");
		}
	}
	

	public String getProjectName() {
		File temp = new File(sourceFolder);
		if (temp.getName().equals("src") || temp.getName().equals("source")) {
			return new File(temp.getParent()).getName();
		} else {
			return new File(sourceFolder).getName();
		}
	}
}
