package org.osate.iso26262.fmeda;

import java.util.ArrayList;
import java.util.List;

import org.osate.iso26262.fmeda.util.CalculationUtil;

public class FmedaTable {

	public String blockName;
	public String safetyGoal;
	public String ASIL;

	public List<FmedaProperty> fpList;

	public Double totalSPRF;
	public Double totalMPFL;

	public Double totalFailureRate;
	public Double totalSafetyRelated;
	public Double totalNonSafetyRelated;

	public Double SPFM;
	public Double LFM;
	public Double PMHF;

	public Boolean reachASILLevel;

	public FmedaTable() {
		// default value
		this.blockName = "unknown";
		this.safetyGoal = "";
		this.ASIL = "";
		this.fpList = new ArrayList<FmedaProperty>();
		this.CalcInit();
	}

	/**
	 * Add a FmedaProperty
	**/
	public void addFmedaProperty(FmedaProperty fp) {
		this.fpList.add(fp);
	}

	/**
	 * Set FmedaProperty list
	**/
	public void setFmedaPropertyList(List<FmedaProperty> fpList) {
		this.fpList = fpList;
	}

	/**
	 * Calculation initialization
	**/
	public void CalcInit() {
		this.totalSPRF = Double.valueOf(0);
		this.totalMPFL = Double.valueOf(0);
		this.totalFailureRate = Double.valueOf(0);
		this.totalSafetyRelated = Double.valueOf(0);
		this.totalNonSafetyRelated = Double.valueOf(0);
		this.SPFM = Double.valueOf(0);
		this.LFM = Double.valueOf(0);
		this.PMHF = Double.valueOf(0);
		this.reachASILLevel = false;
	}

	/**
	 * Calculate FMEDA index
	**/
	public void fmedaCalc() {
		CalculationUtil.fmedaCalc(this);
	}

	/**
	 * Determine if table is empty
	**/
	public Boolean isEmpty() {
		return this.fpList.isEmpty();
	}

}
