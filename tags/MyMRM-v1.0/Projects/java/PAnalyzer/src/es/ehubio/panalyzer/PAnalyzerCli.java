package es.ehubio.panalyzer;

import javax.xml.bind.JAXBException;

public class PAnalyzerCli {
	public static void main( String[] args ) throws JAXBException {
		if( args.length != 1 ) {
			showUsage();
			return;
		}
		
		MainModel model = new MainModel();
		model.run(args[0]);
	}

	private static void showUsage() {
		System.out.println(String.format(
			"Usage:\n\tjava -cp %s %s <experiment.pax>", "<lib.jar>", PAnalyzerCli.class.getName()));
	}
}
