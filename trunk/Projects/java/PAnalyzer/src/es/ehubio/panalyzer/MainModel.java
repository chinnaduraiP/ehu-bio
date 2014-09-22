package es.ehubio.panalyzer;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MainModel {
	public static final String SIGNATURE = "PAnalyzer (v2.0-alpha1)";
	private final StringWriter logString = new StringWriter();
	private final PrintWriter log = new PrintWriter(logString);
	private String status = "Load experiment data";
	
	public MainModel() {
		showWelcome();
		showUsage();
	}	

	private void showWelcome() {
		log.println(String.format("--- Welcome to %s ---", SIGNATURE));		
	}
	
	private void showUsage() {
		log.println("\nIn a normal execution you should follow these steps:");
		log.println("1. Load experiment file(s) in the 'Experiment' tab");
		log.println("2. Apply some quality criteria in the 'Filter' tab");
		log.println("3. Check and export the results in the 'Results' tab");
		log.println("4. Browse the results using the integrated 'Browser' tab or your favorite web browser outside this application");
	}

	public String getStatus() {
		return status;
	}
	
	public String getLog() {
		return logString.toString();
	}
}
