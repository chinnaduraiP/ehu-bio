package es.ehubio.panalyzer;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

class HtmlReport {
	private static final String DATA = "html_data";
	private static final String CONFIG = "config.html";
	private static final String RESULTS = "results.html";
	private static final String PROTEINS = "proteins.html";
	private static final int TAB_CONFIG = 1;
	private static final int TAB_RESULTS = 2;
	private static final int TAB_PROTEINS = 3;
	private static final int TAB_DETAILS = 4;
	
	private final MainModel model;
	private File htmlFile;
	
	public HtmlReport( MainModel model ) {
		this.model = model;		
	}
	
	public void create() throws IOException {
		File dir = new File(model.getConfig().getOutput()).getParentFile();		
		File data = new File(dir,DATA);
		data.mkdir();
		
		htmlFile = new File(dir,String.format("%s.html", model.getConfig().getOutput()));
		writeFile(data, "index.html", "MyExperiment", TAB_CONFIG, "Hola <b>prueba</b>");
	}

	public File getHtmlFile() {
		return htmlFile;
	}
	
	private void writeFile( File dir, String file, String title, int tab, String content ) throws IOException {
		StringWriter buffer = new StringWriter();
		Reader input = new InputStreamReader(HtmlReport.class.getResource("skel.html").openStream());
		Writer output = new PrintWriter(buffer);
		IOUtils.copy(input, output);
		input.close();
		output.close();
		String str = buffer.toString();
		str = str.replaceAll("@EXPERIMENT", title);
		str = str.replaceAll("@DETAILS", file);
		str = str.replaceAll("@CONTENT", content);
		str = str.replaceAll("@NUMBER", tab+"");
		PrintWriter pw = new PrintWriter(new File(dir, file));
		pw.print(str);
		pw.close();
	}
}
