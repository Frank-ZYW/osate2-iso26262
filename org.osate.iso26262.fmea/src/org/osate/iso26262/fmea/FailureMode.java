package org.osate.iso26262.fmea;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.IntegerLiteral;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.ModalPropertyValue;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.StringLiteral;
import org.osate.xtext.aadl2.errormodel.errorModel.EMV2PropertyAssociation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.util.EMV2Properties;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;
import org.osate.xtext.aadl2.properties.util.GetProperties;

public class FailureMode {
	public String id;
	public String name;
	public ErrorBehaviorState failure_mode;
	public Structure ref_component;
	public Function ref_func;
	public List<FailureMode> failure_effect = new ArrayList<FailureMode>();
	public List<FailureMode> failure_cause = new ArrayList<FailureMode>();

	public Integer serverity;
	public Integer occurrence;
	public Integer detection;
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
		System.out.println("	new FailureMode::" + EMV2Util.getPrintName(ref_component.ci) + "." + id + ":::" + name);

		this.id = id;
		this.name = name;
		this.failure_mode = failure_mode;
		this.ref_component = ref_component;
		this.ref_func = ref_func;

		this.serverity = serverity;
		this.occurrence = occurrence;
		this.detection = detection;
		this.prevention_control = prevention_control;
		this.detection_control = detection_control;
		Fill_Optimizations();
	}


	public void Print(String indent) {
		System.out.print(indent + "  |<-Structure::" + ref_component.ci.getName());
		System.out.print("  |id:: " + id);
		System.out.print("  |state name:: " + EMV2Util.getPrintName(failure_mode));
		System.out.print("  |name:: " + name);
		System.out.print("  |ref_func:: " + ref_func.id);
		if (serverity != null) {
			System.out.print("  ||S:: " + serverity);
		}
		if (ref_S != null) {
			System.out.print("  ||ref_S:: " + ref_S);
		}
		if (occurrence != null) {
			System.out.print("  ||O:: " + occurrence);
		}
		if (detection != null) {
			System.out.print("  ||D:: " + detection);
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
				System.out.print(fi.ref_component.ci.getName() + "." + fi.id + " , ");
			}
			System.out.print(")<<---");
		}
		if (failure_cause.size() > 0) {
			System.out.print("  |--->>(");
			for (FailureMode fi : failure_cause) {
				System.out.print(fi.ref_component.ci.getName() + "." + fi.id + " , ");
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

	public void Fill_Optimizations() {

		List<EMV2PropertyAssociation> fm = EMV2Properties.getProperty("FMEA_Prop::Optimization", ref_component.ci,
				failure_mode,
				failure_mode.getTypeSet());

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
					BasicPropertyAssociation xref = GetProperties.getRecordField(fields, "OPT_PC");
					if (xref != null) {
						PropertyExpression val = xref.getOwnedValue();
						OPT_PC = ((StringLiteral) val).getValue();
						xref = null;
					}

					// get OPT_DC
					xref = GetProperties.getRecordField(fields, "OPT_DC");
					if (xref != null) {
						PropertyExpression val = xref.getOwnedValue();
						OPT_DC = ((StringLiteral) val).getValue();
						xref = null;
					}

					// get Respons_Person
					xref = GetProperties.getRecordField(fields, "Respons_Person");
					if (xref != null) {
						PropertyExpression val = xref.getOwnedValue();
						Respons_Person = ((StringLiteral) val).getValue();
						xref = null;
					}

					// get Target_Completion_Data
					xref = GetProperties.getRecordField(fields, "Target_Completion_Data");
					if (xref != null) {
						PropertyExpression val = xref.getOwnedValue();
						Target_Completion_Data = ((StringLiteral) val).getValue();
						xref = null;
					}

					// get Status
					xref = GetProperties.getRecordField(fields, "Status");
					if (xref != null) {
						PropertyExpression val = xref.getOwnedValue();
						Status = ((StringLiteral) val).getValue();
						xref = null;
					}

					// get Evidence
					xref = GetProperties.getRecordField(fields, "Evidence");
					if (xref != null) {
						PropertyExpression val = xref.getOwnedValue();
						Evidence = ((StringLiteral) val).getValue();
						xref = null;
					}

					// get Completion_Data
					xref = GetProperties.getRecordField(fields, "Completion_Data");
					if (xref != null) {
						PropertyExpression val = xref.getOwnedValue();
						Completion_Data = ((StringLiteral) val).getValue();
						xref = null;
					}

					// get OPT_Occurrence
					xref = GetProperties.getRecordField(fields, "OPT_Occurrence");
					if (xref != null) {
						PropertyExpression val = xref.getOwnedValue();
						OPT_Occurrence = (int) ((IntegerLiteral) val).getValue();
						xref = null;
					}

					// get OPT_Detection
					xref = GetProperties.getRecordField(fields, "OPT_Detection");
					if (xref != null) {
						PropertyExpression val = xref.getOwnedValue();
						OPT_Detection = (int) ((IntegerLiteral) val).getValue();
						xref = null;
					}
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
		if (serverity != null) {
			maxS = serverity;
		}
		for(FailureMode fi:failure_effect)
		{
			maxS = Math.max(maxS, fi.SearchMaxrefS());
		}
		if (occurrence != null && detection != null && maxS != 0) {
			ref_S=maxS;
		}
		return maxS;
	}

	public void Cal_AP()
	{
		if(this.occurrence!=null&&this.detection!=null&&this.ref_S!=null) {
			this.myap=FmeaBuilder.CalculateAp(ref_S, occurrence, detection);
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
