package es.ehubio.mymrm.presentation;

import java.io.Serializable;

import es.ehubio.mymrm.data.Fragment;
import es.ehubio.proteomics.IonType;

public class FragmentBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private Fragment entity;
	private String name;
	
	public Fragment getEntity() {
		return entity;
	}
	
	public void setEntity(Fragment entity) {
		this.entity = entity;
		IonType type = IonType.getByName(entity.getIonType().getName());
		if( type == null ) {
			name = entity.getIonType().getName();
			return;
		}
		name = type.format(entity.getPosition());
	}
	
	public String getName() {
		return name;
	}
}
