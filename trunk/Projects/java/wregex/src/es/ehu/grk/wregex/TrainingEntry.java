package es.ehu.grk.wregex;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import es.ehu.grk.db.Fasta;
import es.ehu.grk.db.Fasta.InvalidSequenceException;
import es.ehu.grk.db.Fasta.SequenceType;

public final class TrainingEntry {	
	/**
	 * Motif positions are read if the last part of the header is in the form
	 * x-y;z-... Is this pattern is not found, the whole sequence is interpreted
	 * as the motif
	 */
	public TrainingEntry(Fasta fasta) {
		mFasta = fasta;
		mId = fasta.guessAccession();
		loadMotifs();
		if (mMotifs.isEmpty())
			mMotifs.add(new InputMotif(mFasta, 1, mFasta.sequence().length(), 100.0));
	}

	private void loadMotifs() {
		String[] fields = mFasta.header().split("[ \t]");
		if (fields.length == 1)
			return;
		String str = fields[fields.length - 1];
		String valid = "0123456789;-@.";
		for (char c : str.toCharArray())
			if (valid.indexOf(c) == -1)
				return;
		int start, end;
		double w;
		String[] tmp;
		for (String range : str.split(";")) {
			fields = range.split("-");
			if (fields.length != 2)
				return;
			start = Integer.parseInt(fields[0]);
			tmp = fields[1].split("@");
			end = Integer.parseInt(tmp[0]);
			w = tmp.length != 2 ? 100.0 : Double.parseDouble(tmp[1]);
			mMotifs.add(new InputMotif(mFasta, start, end, w));
		}
	}

	public String getSequence() {
		return mFasta.sequence();
	}

	public String getHeader() {
		return mFasta.header();
	}

	public List<InputMotif> getMotifs() {
		return mMotifs;
	}

	/** The first word of the fasta header */
	public String getId() {
		return mId;
	}

	public static List<TrainingEntry> readEntries(Reader rd) throws IOException,
			InvalidSequenceException {
		List<TrainingEntry> list = new ArrayList<TrainingEntry>();
		for (Fasta f : Fasta.readEntries(rd, SequenceType.PROTEIN))
			list.add(new TrainingEntry(f));
		return list;
	}

	public static void writeEntries(Writer wr, Iterable<TrainingEntry> list) {
		PrintWriter pw = new PrintWriter(wr);
		boolean first;
		for (TrainingEntry entry : list) {
			pw.print(">" + entry.getId() + " ");
			first = true;
			for (InputMotif motif : entry.getMotifs()) {
				if (first)
					first = false;
				else
					pw.print(';');
				pw.print((motif.getStart()+1) + "-" + (motif.getEnd()+1));
			}
			pw.println();
			pw.println(entry.getSequence());
		}
	}

	private final Fasta mFasta;
	private final String mId;
	private final List<InputMotif> mMotifs = new ArrayList<InputMotif>();
}