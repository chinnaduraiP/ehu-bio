package es.ehu.grk.wregex.gae;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class WregexBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String regex;

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}
}
