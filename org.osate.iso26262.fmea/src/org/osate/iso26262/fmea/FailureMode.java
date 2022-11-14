package org.osate.iso26262.fmea;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.ModalPropertyValue;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RecordValue;
import org.osate.xtext.aadl2.errormodel.errorModel.EMV2PropertyAssociation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeSet;
import org.osate.xtext.aadl2.errormodel.util.EMV2Properties;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;

public class FailureMode {
	public String id;
	public String name;
	public String description;
	public String mishap;
	public String evironment;
	public String verifivation;
	public String crossreference;
	public String comment;
	public Double probability;

	public Integer asil_serverity;
	public Integer asil_exposure;
	public Integer asil_controllability;
	public String serverity_comment;
	public String exposure_comment;
	public String controllability_comment;
	public ASIL asil;

	public NamedElement failure_mode;
	public TypeSet failure_mode_typeset;
	public Structure ref_component;
	public Function ref_func;
	public List<FailureMode> failure_effect = new ArrayList<FailureMode>();
	public List<FailureMode> failure_cause = new ArrayList<FailureMode>();

	public Integer fmea_serverity;
	public Integer fmea_occurrence;
	public Integer fmea_detection;
	public String prevention_control;
	public String detection_control;


	private AP myap = AP.Emp;
	public Integer ref_S;

	public List<Optimization> optimizations = new ArrayList<Optimization>();

	public AP getMyap() {
		return myap;
	}

	public void setMyap(AP myap) {
		this.myap = myap;
	};

	FailureMode(String id, String name, ErrorBehaviorState failure_mode, Structure ref_component, Function ref_func,
			Integer serverity, Integer occurrence, Integer detection, String prevention_control,
			String detection_control) {

		this.ref_component = ref_component;
		this.ref_func = ref_func;

	}

	FailureMode(NamedElement ci, NamedElement target, TypeSet ts) {

		this.failure_mode = target;
		this.failure_mode_typeset = ts;
		this.id = EMV2Util.getPrintName(target) + FmeaBuilder.TypeSetName(ts);

		List<EMV2PropertyAssociation> fm = EMV2Properties.getProperty("ISO26262::Hazards", ci, target, ts);
		EMV2PropertyAssociation fma = fm.isEmpty() ? null : fm.get(0);
		PropertyExpression fmv = EMV2Properties.getPropertyValue(fma);
		EList<BasicPropertyAssociation> fields = fmv == null ? null
				: ((RecordValue) ((ListValue) fmv).getOwnedListElements().get(0)).getOwnedFieldValues();
		if (fields != null) {

			this.name = FmeaBuilder.getRecordStringProperty(fields, "HazardName");
			this.description = FmeaBuilder.getRecordStringProperty(fields, "Description");
			this.mishap = FmeaBuilder.getRecordStringProperty(fields, "Mishap");
			this.evironment = FmeaBuilder.getRecordStringProperty(fields, "Evironment");
			this.verifivation = FmeaBuilder.getRecordStringProperty(fields, "VerificationMethod");
			this.crossreference = FmeaBuilder.getRecordStringProperty(fields, "CrossReference");
			this.comment = FmeaBuilder.getRecordStringProperty(fields, "Comment");
			this.probability = FmeaBuilder.getRecordRealProperty(fields, "Probability");

			this.asil_serverity = FmeaBuilder.getRecordIntProperty(fields, "Severity");
			this.serverity_comment = FmeaBuilder.getRecordStringProperty(fields, "SeverityComment");
			this.asil_exposure = FmeaBuilder.getRecordIntProperty(fields, "Exposure");
			this.exposure_comment = FmeaBuilder.getRecordStringProperty(fields, "ExposureComment");
			this.asil_controllability = FmeaBuilder.getRecordIntProperty(fields, "Controllability");
			this.controllability_comment = FmeaBuilder.getRecordStringProperty(fields, "ControllabilityComment");
			this.asil = ASIL.findName(FmeaBuilder.getRecordEnumerationProperty(fields, "ASIL"));
		}
		if (this.name == null) {
			this.name = ci.getName() + "." + id;
		}
		fm = EMV2Properties.getProperty("ISO26262::FmeaRiskAnalysis", ci, target, ts);
		fma = fm.isEmpty() ? null : fm.get(0);
		fmv = EMV2Properties.getPropertyValue(fma);
		fields = fmv == null ? null
				: ((RecordValue) fmv).getOwnedFieldValues();
		if (fields != null) {
			this.fmea_serverity = FmeaBuilder.getRecordIntProperty(fields, "Severity");
			this.fmea_occurrence = FmeaBuilder.getRecordIntProperty(fields, "Occurrence");
			this.fmea_detection = FmeaBuilder.getRecordIntProperty(fields, "Detection");
			this.prevention_control = FmeaBuilder.getRecordStringProperty(fields, "PC");
			this.detection_control = FmeaBuilder.getRecordStringProperty(fields, "DC");
		}
		Fill_Optimizations(ci, target, ts);

	}

	public void Print(String indent) {
		System.out.print(indent + "  |<-Structure::" + ref_component.getName());
		System.out.print("  |id:: " + id);
		System.out.print("  |name:: " + name);
		System.out.print("  |ref_func:: " + ref_func.id);
		if (fmea_serverity != null) {
			System.out.print("  ||S:: " + fmea_serverity);
		}
		if (ref_S != null) {
			System.out.print("  ||ref_S:: " + ref_S);
		}
		if (fmea_occurrence != null) {
			System.out.print("  ||O:: " + fmea_occurrence);
		}
		if (fmea_detection != null) {
			System.out.print("  ||D:: " + fmea_detection);
		}
		if (myap != AP.Emp) {
			System.out.print("  ||AP:: " + myap);
		}
		if (prevention_control != null) {
			System.out.print("  ||PC:: " + prevention_control);
		}
		if (detection_control != null) {
			System.out.print("  ||DC:: " + detection_control);
		}
		if (failure_effect.size() > 0) {
			System.out.print("  | (");
			for (FailureMode fi : failure_effect) {
				System.out.print(fi.ref_component.getName() + "." + fi.id + " , ");
			}
			System.out.print(")<<---");
		}
		if (failure_cause.size() > 0) {
			System.out.print("  |--->>(");
			for (FailureMode fi : failure_cause) {
				System.out.print(fi.ref_component.getName() + "." + fi.id + " , ");
			}
			System.out.print(")");
		}
		System.out.println("");
		if (optimizations.size() > 0) {
			System.out.println(indent + "\tOptimizations:");
			for (Optimization oi : optimizations) {
				oi.Print(indent + "\t\t");
			}

		}
	}

	public void Fill_Optimizations(NamedElement ci, NamedElement target, TypeSet ts) {

		List<EMV2PropertyAssociation> fm = EMV2Properties.getProperty("ISO26262::FmeaOptimization", ci, target, ts);

		for (EMV2PropertyAssociation PA : fm) {
			for (ModalPropertyValue modalPropertyValue : PA.getOwnedValues()) {
				PropertyExpression peVal = modalPropertyValue.getOwnedValue();
				ListValue lv = (ListValue) peVal;
				for (PropertyExpression pe : lv.getOwnedListElements()) {
					EList<BasicPropertyAssociation> fields = ((RecordValue) pe).getOwnedFieldValues();
					String OPT_PC = null;
					String OPT_DC = null;
					String Respons_Person = null;
					String Target_Completion_Data = null;
					String Status = null;
					String Evidence = null;
					String Completion_Data = null;
					Integer OPT_Occurrence = null;
					Integer OPT_Detection = null;

					if (fields != null) {
					// get OPT_PC
					OPT_PC = FmeaBuilder.getRecordStringProperty(fields, "OptPC");

					// get OPT_DC
					OPT_DC = FmeaBuilder.getRecordStringProperty(fields, "OptDC");

					// get Respons_Person
					Respons_Person = FmeaBuilder.getRecordStringProperty(fields, "ResponsPerson");

					// get Target_Completion_Data
					Target_Completion_Data = FmeaBuilder.getRecordStringProperty(fields, "TargetCompletionData");

					// get Status
					Status = FmeaBuilder.getRecordStringProperty(fields, "Status");

					// get Evidence
					Evidence = FmeaBuilder.getRecordStringProperty(fields, "Evidence");

					// get Completion_Data
					Completion_Data = FmeaBuilder.getRecordStringProperty(fields, "CompletionData");

					// get OPT_Occurrence
					OPT_Occurrence = FmeaBuilder.getRecordIntProperty(fields, "OPTOccurrence");

					// get OPT_Detection
					OPT_Detection = FmeaBuilder.getRecordIntProperty(fields, "OPTDetection");

					optimizations.add(new Optimization(this, OPT_PC, OPT_DC, Respons_Person, Target_Completion_Data,
							Status, Evidence, Completion_Data, OPT_Occurrence, OPT_Detection));

				}
			}
			}
		}

	}

	public Integer SearchMaxrefS() {
		Integer maxS=0;
		if (ref_S != null) {
			return ref_S;
		}
		if (fmea_serverity != null) {
			maxS = fmea_serverity;
		}
		for(FailureMode fi:failure_effect)
		{
			maxS = Math.max(maxS, fi.SearchMaxrefS());
		}
		if (fmea_occurrence != null && fmea_detection != null && maxS != 0) {
			ref_S=maxS;
		}
		return maxS;
	}

	public void Cal_AP()
	{
		if(this.fmea_occurrence!=null&&this.fmea_detection!=null&&this.ref_S!=null) {
			this.myap=FmeaBuilder.CalculateAp(ref_S, fmea_occurrence, fmea_detection);
		}
			for (Optimization oi : this.optimizations) {
				oi.Cal_AP();
			}

	}

	public void getLeaf(List<FailureMode> list)
	{
		if(failure_cause.size()==0)
		{
			list.add(this);
		}
		else {
			for (FailureMode fmi : failure_cause) {
				fmi.getLeaf(list);
			}
		}
	}




}
