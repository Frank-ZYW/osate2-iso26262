package org.osate.iso26262.fmeda;

import java.util.ArrayList;
import java.util.List;

public class FmedaProperty {

	public String componentName;
	public Double failureRate; // FIT
	public Boolean isSafetyRelated;
	public List<FmedaFaultMode> faultModes;

	public FmedaProperty() {
		// default value
		this.componentName = "unknown";
		this.failureRate = Double.valueOf(0);
		this.isSafetyRelated = false;
		this.faultModes = new ArrayList<FmedaFaultMode>();
	}

	/**
	 * Add a fault mode
	**/
	public void addFmedaFaultMode(FmedaFaultMode fm) {
		this.faultModes.add(fm);
	}

	/**
	 * Set fault modes
	**/
	public void setFmedaFaultModes(List<FmedaFaultMode> fmList) {
		this.faultModes = fmList;
	}

	/**
	 * Calculation initialization
	**/
	public void CalcInit() {
		for (FmedaFaultMode fm : this.faultModes) {
			fm.CalcInit();
		}
	}

}
