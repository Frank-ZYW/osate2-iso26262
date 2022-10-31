package org.osate.iso26262.fmeda;

public class FmedaFaultMode {

	public String modeName;
	public Double distribution; // percentage

	// single point fault
	public Boolean hasSPF;
	public String spf_SM; // Safety Mechanism(s)
	public Double spf_DC; // Diagnostic Coverage

	// multiple point fault
	public Boolean hasMPF;
	public String mpf_SM;
	public Double mpf_DC;

	public Double sprf; // SPF or RF
	public Double mpfl; // MPF,L

	public FmedaFaultMode() {
		// default value
		this.modeName = "unknown";
		this.distribution = Double.valueOf(0);
		this.hasSPF = false;
		this.spf_SM = "none";
		this.spf_DC = Double.valueOf(0);
		this.hasMPF = false;
		this.mpf_SM = "none";
		this.mpf_DC = Double.valueOf(0);
		this.sprf = Double.valueOf(0);
		this.mpfl = Double.valueOf(0);
	}

	/**
	 * Calculation initialization
	**/
	public void CalcInit() {
		this.sprf = Double.valueOf(0);
		this.mpfl = Double.valueOf(0);
	}

}
