package es.ehubio.wregex.view;

import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "targets")
@XmlAccessorType(XmlAccessType.FIELD)
public class TargetConfiguration implements Serializable {
	private static final long serialVersionUID = 1L;
	@XmlElement(name="target")
	private List<TargetInformation> targets;
	
	public List<TargetInformation> getTargets() {
		return targets;
	}
	
	public void setTargets(List<TargetInformation> targets) {
		this.targets = targets;
	}
	
	public static TargetConfiguration load( Reader rd ) {
		TargetConfiguration configuration = null;
		try {
			JAXBContext context = JAXBContext.newInstance(TargetConfiguration.class);
			Unmarshaller um = context.createUnmarshaller();
			configuration = (TargetConfiguration)um.unmarshal(rd);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		return configuration;
	}
	
	public void save( PrintStream writer ) {
		try {
			JAXBContext context = JAXBContext.newInstance(TargetConfiguration.class);
			Marshaller m = context.createMarshaller();
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		    m.marshal(this,writer);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}		
	}
}
