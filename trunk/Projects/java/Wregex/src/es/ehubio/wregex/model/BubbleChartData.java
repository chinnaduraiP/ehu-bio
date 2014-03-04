package es.ehubio.wregex.model;

import java.util.ArrayList;
import java.util.List;

public class BubbleChartData {
	private String name;
	private int size;
	private List<BubbleChartData> children = new ArrayList<>();
	
	public BubbleChartData( String name, int size ) {
		this.name = name;
		this.size = size;
	}
	
	public BubbleChartData( String name ) {
		this(name, 0);
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getTotalSize() {
		if( children == null || children.isEmpty() )
			return getSize();
		int totalSize = 0;
		for( BubbleChartData child : children )
			totalSize += child.getTotalSize();
		return totalSize;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<BubbleChartData> getChildren() {
		return children;
	}
	
	public void setChildren(List<BubbleChartData> children) {
		this.children = children;
	}
	
	public void addChild( BubbleChartData child ) {
		this.children.add(child);
	}
	
	public boolean constainsChild( String name ) {
		return getChild(name) != null;
	}
	
	public BubbleChartData getChild( String name ) {
		for( BubbleChartData child : children )
			if( child.name.equals(name) )
				return child;
		return null;
	}
	
	public String toString(StringBuilder stringBuilder) {
		StringBuilder sb = null;
		
		if (stringBuilder == null) {
			sb = new StringBuilder();
		} else {
			sb = stringBuilder;
		}
		sb.append("{").append("\n").append("\"name\": \"").append(this.getName()).append("\",\n");
		if( this.getChildren() != null ) {
			if( this.getChildren().size() > 0 ) {
				sb.append("\"children\": [\n");				
				for(int i=0; i<this.getChildren().size(); i++) {
					sb.append(this.getChildren().get(i).toString(null));
					if( i<this.getChildren().size()-1 )
						sb.append(",\n");
				}
				sb.append("]\n");
			} else
				sb.append("\"size\" : ").append(this.getSize()).append("\n");
		}
		sb.append("}");
		return sb.toString();
	}
}
