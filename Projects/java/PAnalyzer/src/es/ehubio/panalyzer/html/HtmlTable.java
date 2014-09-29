package es.ehubio.panalyzer.html;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class HtmlTable {
	private String title;
	private List<String> header;
	private List<List<String>> rows = new ArrayList<>();
	private List<String> row;
	private boolean addToHeader = false;
	private boolean odd = true;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void beginHeader() {
		header = new ArrayList<>();
		addToHeader = true;
	}
	
	public void endHeader() {
		addToHeader = false;
	}
	
	public void beginRow() {
		endRow();
		row = new ArrayList<>();
	}

	public void endRow() {
		endHeader();
		if( row != null )
			rows.add(row);
		row = null;		
	}
	
	public void addCell( String text ) {
		if( addToHeader )
			header.add(text);
		else
			row.add(text);
	}

	public String render() {
		endRow();
		StringWriter buffer = new StringWriter();
		PrintWriter pw = new PrintWriter(buffer);
		pw.println("<table>");
		if( title != null )
			pw.println(String.format("<caption>%s</caption>", title));		
		if( header != null && header.size() != 0 )
			pw.println(renderHeader(header));
		for( List<String> row : rows )
			pw.println(renderRow(row));
		pw.println("</table>");
		pw.close();
		return buffer.toString();
	}
	
	private String renderRow( List<String> cells ) {
		return renderAux("td",cells);
	}
	
	private String renderHeader( List<String> cells ) {
		return renderAux("th",cells);
	}
	
	private String renderAux( String tag, List<String> cells ) {
		StringBuilder builder = new StringBuilder(odd ? "<tr class=\"odd\">" : "<tr class=\"even\">");
		odd = !odd;
		for( String cell : cells ) {
			builder.append('<');
			builder.append(tag);
			builder.append('>');
			builder.append(cell);
			builder.append("</");
			builder.append(tag);
			builder.append('>');
		}
		builder.append("</tr>");
		return builder.toString();
	} 
}