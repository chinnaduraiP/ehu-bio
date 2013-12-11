package es.ehu.grk.wregex.view;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="reference")
public final class MotifReference implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String link;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
}
