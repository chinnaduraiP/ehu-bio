package es.ehubio.tools;

import es.ehubio.proteomics.Enzyme;
import es.ehubio.proteomics.pipeline.DecoyDb;

public class DecoyCreator implements Command.Interface {

	@Override
	public String getUsage() {
		return "<target.fasta> <decoy.fasta> <prefix>";
	}

	@Override
	public int getMinArgs() {
		return 3;
	}

	@Override
	public int getMaxArgs() {
		return 3;
	}

	@Override
	public void run(String[] args) throws Exception {
		DecoyDb.create(args[0], args[1], DecoyDb.Strategy.PSEUDO_REVERSE, Enzyme.TRYPSIN_P, args[2]);		
	}

}
