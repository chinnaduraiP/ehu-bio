package es.ehubio.tools;

public class MyMRM implements Command.Interface {
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
	}

}
