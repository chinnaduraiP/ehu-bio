package es.ehubio.mymrm.tools;

import es.ehubio.mymrm.business.Database;
import es.ehubio.proteomics.MsMsData;
import es.ehubio.tools.Command;
import es.ehubio.tools.PAnalyzerCli;

public class Feeder implements Command.Interface {
	private final PAnalyzerCli panalyzer = new PAnalyzerCli();

	@Override
	public String getUsage() {
		return panalyzer.getUsage();
	}

	@Override
	public int getMinArgs() {
		return panalyzer.getMinArgs();
	}

	@Override
	public int getMaxArgs() {
		return panalyzer.getMaxArgs();
	}

	@Override
	public void run(String[] args) throws Exception {
		panalyzer.setLoadIons(true);
		panalyzer.setSaveResults(false);
		panalyzer.run(args);
		feedDb(panalyzer.getData());
	}

	private void feedDb(MsMsData data) {
		Database.connect();
		Database.feed(4,data);
		Database.close();
	}	
}
