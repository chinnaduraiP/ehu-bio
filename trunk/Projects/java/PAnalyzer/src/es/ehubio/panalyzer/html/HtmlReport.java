package es.ehubio.panalyzer.html;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import es.ehubio.io.CsvUtils;
import es.ehubio.panalyzer.MainModel;
import es.ehubio.proteomics.Protein;

public class HtmlReport {
	private static final String DATA = "html";
	private static final String STYLE = "style.css";
	private static final String INDEX = "index.html";
	private static final String CONFIG = "config.html";
	private static final String PROTEINS = "proteins.html";
	private static final int TAB_INDEX = 1;
	private static final int TAB_CONFIG = 2;	
	private static final int TAB_PROTEINS = 3;
	
	private final MainModel model;
	private File htmlDir;
	private File htmlFile;
	
	public HtmlReport( MainModel model ) {
		this.model = model;		
	}
	
	public void create() throws IOException {
		String exp = model.getConfig().getDescription();
		htmlDir = new File(model.getConfig().getOutput(),DATA);		
		htmlDir.mkdir();		
		htmlFile = new File(htmlDir,PROTEINS);
		
		Reader input = new InputStreamReader(HtmlReport.class.getResource(STYLE).openStream());
		Writer output = new PrintWriter(new File(htmlDir,STYLE));
		IOUtils.copy(input, output);
		input.close();
		output.close();
		
		input = new InputStreamReader(HtmlReport.class.getResource(INDEX).openStream());
		output = new PrintWriter(new File(model.getConfig().getOutput(),INDEX));
		IOUtils.copy(input, output);
		input.close();
		output.close();
		
		writeFile(INDEX, String.format("PAnalyzer results for %s",exp), TAB_INDEX, getSummary());
		writeFile(CONFIG, String.format("PAnalyzer configuration for %s",exp), TAB_CONFIG, getConfig());
		writeFile(PROTEINS, String.format("Proteins in %s",exp), TAB_PROTEINS, getProteinList());
		for( Protein protein : model.getData().getProteins() )
			writeFile(
				String.format("%s.html", protein.getAccession()),
				String.format("Protein details for %s", protein.getAccession()),
				TAB_PROTEINS,
				getProteinDetails(protein));
	}			

	public File getHtmlFile() {
		return htmlFile;
	}
	
	private void writeFile( String file, String title, int tab, String content ) throws IOException {
		StringWriter buffer = new StringWriter();
		Reader input = new InputStreamReader(HtmlReport.class.getResource("skel.html").openStream());
		Writer output = new PrintWriter(buffer);
		IOUtils.copy(input, output);
		input.close();
		output.close();
		String str = buffer.toString();
		str = str.replaceAll("@TITLE", title);
		str = str.replaceAll("@TAB", tab+"");
		str = str.replaceAll("@CONTENT", content);		
		PrintWriter pw = new PrintWriter(new File(htmlDir,file));
		pw.print(str);
		pw.close();
	}
	
	private String getConfig() {
		return "";
	}
	
	private String getSummary() {
		return "";
	}
	
	private String getProteinList() {
		List<Protein> proteins = new ArrayList<>(model.getData().getProteins());
		Collections.sort(proteins, new Comparator<Protein>() {
			@Override
			public int compare(Protein o1, Protein o2) {
				int diff = o1.getConfidence().compareTo(o2.getConfidence());
				if( diff != 0 )
					return diff;
				return o1.getAccession().compareToIgnoreCase(o2.getAccession());
			}
		});
		HtmlTable table = new HtmlTable();
		table.setTitle("Protein List");
		table.beginHeader();
		table.addCell("Accession");
		//table.addCell("Name");
		table.addCell("Evidence");
		table.addCell("Peptide list (unique, discriminating*, non-discriminating**)");
		table.addCell("Description");
		table.endHeader();
		for( Protein protein : proteins ) {
			table.beginRow();
			table.addCell(String.format("<a href=\"%1$s.html\">%1$s</a>", protein.getAccession()));
			//table.addCell(protein.getName());
			table.addCell(protein.getConfidence().toString());
			table.addCell(CsvUtils.getCsv(", ", protein.getPeptides().toArray()));
			table.addCell(trim(protein.getDescription(),120));
			table.endRow();
		}
		return table.render();
	}
	
	private String getProteinDetails(Protein protein) {
		return "";
	}
	
	private String trim( String str, int max ) {
		if( str.length() < max )
			return str;
		return String.format("%s...", str.substring(0,max-2));
	}
}