package org.osate.iso26262.fmea;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.errormodel.FaultTree.Event;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.iso26262.fmea.fixfta.FaultTreeUtils;
import org.osate.xtext.aadl2.errormodel.errorModel.EMV2PropertyAssociation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorTypes;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeToken;
import org.osate.xtext.aadl2.errormodel.util.EMV2Properties;

public class Function {
	public String id;
	public String funcname;
	public SafetyCategory safetycategory;
	public String operatmode;
	public Double ftti;
	public String safestate;
	public Double missiontime;

	public Structure ref_component;
	public NamedElement failure_mode;
	public ErrorTypes failure_mode_typeset;

	public List<Function> func_effect = new ArrayList<Function>();
	public List<Function> func_cause = new ArrayList<Function>();
	public List<FailureElement> ref_fail_modes = new ArrayList<FailureElement>();




	public Function(Event ev) {
		ComponentInstance ci = (ComponentInstance) ev.getRelatedInstanceObject();
		NamedElement target = (NamedElement) ev.getRelatedEMV2Object();
		ErrorTypes ts = null;
		if ((TypeToken) ev.getRelatedErrorType() != null) {
			ts = ((TypeToken) ev.getRelatedErrorType()).getType().get(0);
		}

		this.failure_mode = target;
		this.failure_mode_typeset = ts;

		List<EMV2PropertyAssociation> fm = EMV2Properties.getProperty("ISO26262::Hazards", ci, target, ts);
		EMV2PropertyAssociation fma = fm.isEmpty() ? null : fm.get(0);
		PropertyExpression fmv = EMV2Properties.getPropertyValue(fma);
		EList<BasicPropertyAssociation> fields = fmv == null ? null
				: ((RecordValue) ((ListValue) fmv).getOwnedListElements().get(0)).getOwnedFieldValues();
		if (fields != null) {
			this.funcname = FmeaBuilder.getRecordStringProperty(fields, "SafetyDescription");
			this.safetycategory = SafetyCategory
					.findName(FmeaBuilder.getRecordEnumerationProperty(fields, "SafetyCategory"));
			this.operatmode = FmeaBuilder.getRecordStringProperty(fields, "OperatMode");
			this.ftti = FmeaBuilder.getRecordUnitProperty(fields, "FTTI", "ms");
			this.safestate = FmeaBuilder.getRecordStringProperty(fields, "SafeState");
			this.missiontime = FmeaBuilder.getRecordUnitProperty(fields, "MissionTime", "hr");
		}
		if (this.funcname == null) {
			this.funcname = "Function of " + FaultTreeUtils.getDescription(ev);
		}

	}


	public static Function Check_Merge(Function f1, Function f2)
	{
		boolean tag = true;
		tag &= checkandmerge(f1.safetycategory, f2.safetycategory);
		tag &= checkandmerge(f1.operatmode, f2.operatmode);
		tag &= checkandmerge(f1.ftti, f2.ftti);
		tag &= checkandmerge(f1.safestate, f2.safestate);
		tag &= checkandmerge(f1.missiontime, f2.missiontime);
		if (tag == false) {
			FmeaBuilder.Dialog("Check_Merge",
					"Function \"" + f1.funcname + "\" is inconsistent in " + f1.ref_component.getName());
		}
		return f1;
	}

	public static boolean checkandmerge(Object o1, Object o2) {
		if (o2 != null) {
			if (o1 != null) {
				if (!o1.equals(o2)) {
					return false;
				}
			} else {
				o1 = o2;
			}
		}
		return true;
	}


	public void Print(String indent) {
		System.out.print(indent + "  |<-Structure:: " + ref_component.getName());
		System.out.print("  |id:: " + id);
		System.out.print("  |descri:: " + funcname);
		if (func_effect.size() > 0) {
			System.out.print("  | (");
			for (Function fi : func_effect) {
				System.out.print(fi.ref_component.getName() + "." + fi.id + " , ");
			}
			System.out.print(")<<---");
		}
		if (func_cause.size() > 0) {
			System.out.print("  |--->>(");
			for (Function fi : func_cause) {
				System.out.print(fi.ref_component.getName() + "." + fi.id + " , ");
			}
			System.out.print(")");
		}
		System.out.println("");
		if (ref_fail_modes.size() > 0) {
			System.out.println(indent + "\tRef_fail_modes::");
			for (FailureElement fmi : ref_fail_modes) {
				fmi.Print(indent + "\t\t");
			}
		}
	}


}

