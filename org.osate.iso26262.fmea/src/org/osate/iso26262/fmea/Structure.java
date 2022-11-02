package org.osate.iso26262.fmea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.IntegerLiteral;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.Property;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.StringLiteral;
import org.osate.aadl2.errormodel.FaultTree.Event;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.properties.PropertyNotPresentException;
import org.osate.ui.dialogs.Dialog;
import org.osate.xtext.aadl2.errormodel.errorModel.EMV2PropertyAssociation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.util.EMV2Properties;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;
import org.osate.xtext.aadl2.properties.util.GetProperties;

public class Structure {

	public ComponentInstance ci;
	public Structure high_level_component;
	public HashMap<String, Structure> low_level_components_map = new HashMap<String, Structure>();

	public HashMap<String, Function> functions = new HashMap<String, Function>();
	public HashMap<String, FailureMode> failure_modes = new HashMap<String, FailureMode>();

	public Structure(Structure high, ComponentInstance ci) {
		System.out.println("new Structure::" + ci.getName());
		this.high_level_component = high;
		this.ci = ci;
		Fill_functions();
		Fill_failure_modes();
	}

	public void Print(String indent) {
		System.out.print(indent + "Component :: " + ci.getName());
		if (high_level_component != null) {
			System.out.print("  | (" + high_level_component.ci.getName() + ")<<---");
		}
		if (low_level_components_map.size() > 0) {
			System.out.print("  |--->>(");
		for (String key : low_level_components_map.keySet()) {
				System.out.print(low_level_components_map.get(key).ci.getName() + ",");
			}
			System.out.print(")");
		}
		System.out.println("");
		if (functions.size() > 0) {
		System.out.println(indent + "\tFunctions::");
		for (String key : functions.keySet()) {
			functions.get(key).Print(indent + "\t\t");
		}
	}
		if (failure_modes.size() > 0) {
		System.out.println(indent + "\tFailure_modes::");
		for (String key : failure_modes.keySet()) {
			failure_modes.get(key).Print(indent + "\t\t");
		}
	}
	System.out.println("");

	}

	public void Fill_functions() {

		Property property;
		List<? extends PropertyExpression> propertyValues;
		List<? extends PropertyExpression> propertyValues2;
		BasicPropertyAssociation pa;

		property = GetProperties.lookupPropertyDefinition(ci, "FMEA_Prop", "Function");
		propertyValues = ci.getPropertyValueList(property);

		for (PropertyExpression pe : propertyValues) {
			RecordValue rv = (RecordValue) pe;
			EList<BasicPropertyAssociation> fields = rv.getOwnedFieldValues();

			String id = null;
			String myfunc = null;
			List<String> func_ref = new ArrayList<String>();

			pa = GetProperties.getRecordField(fields, "Func_ID");
			if (pa != null) {
				id = ((StringLiteral) pa.getValue()).getValue();
			}

			pa = GetProperties.getRecordField(fields, "Func_Descrip");
			if (pa != null) {
				myfunc = ((StringLiteral) pa.getValue()).getValue();
			}

			pa = GetProperties.getRecordField(fields, "Func_Require");
			if (pa != null && pa.getValue() instanceof ListValue) {
				propertyValues2 = ((ListValue) pa.getValue()).getOwnedListElements();
				for (PropertyExpression pe2 : propertyValues2) {
					func_ref.add(((StringLiteral) pe2).getValue());
				}
			}

			functions.put(id, new Function(id, myfunc, this, func_ref));
		}
	}

	public void Fill_failure_modes() {

		for (ErrorBehaviorState es : EMV2Util.getAllErrorBehaviorStates(ci)) {
			String id;
			String mode_name = null;
			Function ref_func = null;
			Integer severity = null;
			Integer occurrence = null;
			Integer detection = null;
			String prevention_control = null;
			String detection_control = null;



			// get id
			id = EMV2Util.getPrintName(es);

			// -------------Failure_Mode---------------------
			List<EMV2PropertyAssociation> fm = EMV2Properties.getProperty("FMEA_Prop::Failure_Mode", ci, es,
					es.getTypeSet());
			EMV2PropertyAssociation fma = fm.isEmpty() ? null : fm.get(0);
			PropertyExpression fmv = EMV2Properties.getPropertyValue(fma);
			EList<BasicPropertyAssociation> fields = fmv == null ? null : ((RecordValue) fmv).getOwnedFieldValues();
//			System.out.println("field::::::::" + fields != null ? "OK" : "null");
			if (fields != null) {
				// get Mode_Name
				BasicPropertyAssociation xref = GetProperties.getRecordField(fields, "Mode_Name");
				if (xref != null) {
					PropertyExpression val = xref.getOwnedValue();
					mode_name = ((StringLiteral) val).getValue();
					xref = null;
				}
				// get Link_Func
				xref = GetProperties.getRecordField(fields, "Link_Func");
				if (xref != null) {
					PropertyExpression val = xref.getOwnedValue();
					ref_func = functions.get(((StringLiteral) val).getValue());
					if (ref_func == null) {
						Dialog.showInfo("Fill_failure_modes",
								"FuncRequire \"" + ((StringLiteral) val).getValue() + "\" in " + this.ci.getName() + "."
										+ es.getName() + ":\n Don't have Function \"" + ((StringLiteral) val).getValue()
										+ "\" in " + this.ci.getName());

					}
					xref = null;
				}
			}
			// -------------Failure_Mode----------------------------


			// -------------Risk_Analysis---------------------

			fm = EMV2Properties.getProperty("FMEA_Prop::Risk_Analysis", ci, es, es.getTypeSet());
			fma = fm.isEmpty() ? null : fm.get(0);
			fmv = EMV2Properties.getPropertyValue(fma);
			fields = fmv == null ? null : ((RecordValue) fmv).getOwnedFieldValues();
			if (fields != null) {
				// get severity
				BasicPropertyAssociation xref = GetProperties.getRecordField(fields, "Severity");

				if (xref != null) {
					PropertyExpression val = xref.getOwnedValue();
					severity = (int) ((IntegerLiteral) val).getValue();
					xref = null;
				}
				// get occurrence
				xref = GetProperties.getRecordField(fields, "Occurrence");
				if (xref != null) {
					PropertyExpression val = xref.getOwnedValue();
					occurrence = (int) ((IntegerLiteral) val).getValue();
					xref = null;
				}
				// get Detection
				xref = GetProperties.getRecordField(fields, "detection");
				if (xref != null) {
					PropertyExpression val = xref.getOwnedValue();
					detection = (int) ((IntegerLiteral) val).getValue();
					xref = null;
				}

				// get prevention_control
				xref = GetProperties.getRecordField(fields, "PC");
				if (xref != null) {
					PropertyExpression val = xref.getOwnedValue();
					prevention_control = ((StringLiteral) val).getValue();
					xref = null;
				}

				// get detection_control
				xref = GetProperties.getRecordField(fields, "DC");
				if (xref != null) {
					PropertyExpression val = xref.getOwnedValue();
					detection_control = ((StringLiteral) val).getValue();
					xref = null;
				}
			}

			// -------------Risk_Analysis----------------------------
			failure_modes.put(id, new FailureMode(id, mode_name, es, this, ref_func, severity, occurrence, detection,
					prevention_control, detection_control));
			if (ref_func != null) {
				functions.get(ref_func.id).ref_fail_modes.add(failure_modes.get(id));
			}

		}

	}

	public void LinkFunc() {
		for (Function fi : functions.values()) {
			for (String fri : fi.func_require) {
//				System.out.println("    " + this.ci.getName() + " :: " + fi.id + " :: " + fri);
				List<String> reflist = fi.SplitFuncRequire(fri);
				List<String> componentlist = reflist.subList(0, reflist.size() - 1);
				String funcid = reflist.get(reflist.size() - 1);
				Structure current = this;
				for (String cc : componentlist) {
					if (current.low_level_components_map.containsKey(cc)) {
						current = current.low_level_components_map.get(cc);
					} else {
						Dialog.showInfo("LinkFunc", "FuncRequire \"" + fri + "\" in " + this.ci.getName() + "-" + fi.id
								+ ":\n Don't have subcmponent \"" + cc + "\" in " + current.ci.getName());
//						return;
					}
				}
				if(current.functions.containsKey(funcid))
				{
					fi.func_cause.add(current.functions.get(funcid));
					current.functions.get(funcid).func_effect.add(fi);
					System.out.println("add Func link::  " + this.ci.getName() + "." + fi.id + " <==> " + fri);

				} else {
					Dialog.showInfo("LinkFunc", "FuncRequire \"" + fri + "\" in " + this.ci.getName() + "-" + fi.id
							+ ":\n Don't have Function \"" + funcid + "\" in " + current.ci.getName());
//					return;
				}
			}
		}
	}

	public void LinkFTAevent(Event rootevent, FailureMode fm) {
		boolean samecomponent=((ComponentInstance) rootevent.getRelatedInstanceObject()).getName().equals(ci.getName());
		if (samecomponent) {
				for (Event ei : rootevent.getSubEvents()) {
					if (isIntermediateevent(ei)) {
						LinkFTAevent(ei,fm);
					} else {
						Structure linkc = Findstructure(((InstanceObject) ei.getRelatedInstanceObject()).getName());
						if (linkc != null) {
							FailureMode tfm = null;
							EObject nne = ei.getRelatedEMV2Object();
							tfm = linkc.failure_modes.get(EMV2Util.getPrintName((NamedElement) nne));

							if (tfm != null) {
								if (!Islinked(fm, tfm)) {
								fm.failure_cause.add(tfm);
								tfm.failure_effect.add(fm);
								System.out.println("add Error link::  " + fm.ref_component.ci.getName() + "." + fm.id
										+ " <==> " + tfm.ref_component.ci.getName() + "." + tfm.id);
							}
						} else {
								Dialog.showInfo("LinkFTAevent",
										"Component \"" + linkc.ci.getName() + "\" don't have failuremode::"
												+ ((NamedElement) ei.getRelatedEMV2Object()).getName());
							}

							linkc.LinkFTAevent(ei, tfm);
						} else {
							Dialog.showInfo("LinkFTAevent",
									"Can't find Sub Component \""
											+ ((InstanceObject) ei.getRelatedInstanceObject()).getName() + "\" in "
											+ ci.getName());
						}

					}
				}
		} else
		{
			Dialog.showInfo("LinkFTAevent", "not samecomponent");
		}
	}

	public boolean Islinked(FailureMode sup, FailureMode sub) {
		boolean result1 = false;
		boolean result2 = false;
		for (FailureMode fi : sup.failure_cause) {
			if (fi == sub) {
				result1 = true;
			}
		}
		for (FailureMode fi : sub.failure_effect) {
			if (fi == sup) {
				result2 = true;
			}
		}
		if (result1 != result2) {
			Dialog.showInfo("Islinked", "One -way link");
		}
			return result1;

	}

	public Structure Findstructure(String name) {
		Structure result = null;
		boolean same = this.ci.getName().equals(name);
//		System.out.println(
//				"Search:::" + (same ? "same" : "notsame") + "  \"" + this.ci.getName() + "\"--\"" + name + "\"");
		if (same) {
			result = this;
		} else {
			for (Structure cii : low_level_components_map.values()) {
				result = cii.Findstructure(name);
				if (result != null) {
					return result;
				}
			}
		}
		return result;
	}

	public boolean isIntermediateevent(Event e)
	{
		boolean result=false;
		if(e.getName().length()>=12&&e.getName().substring(0, 12).equals("Intermediate")) {
			result=true;
		}
		return result;
	}

	public FmeaHead getHeadPropertie() {
		FmeaHead head = new FmeaHead();
		head.ref_component = this;

		Property property;
		PropertyExpression propertyValue = null;
		BasicPropertyAssociation pa;
		property = GetProperties.lookupPropertyDefinition(ci, "FMEA_Prop", "FMEA_Head");
		try {
		propertyValue = ci.getSimplePropertyValue(property);
	} catch (PropertyNotPresentException e) {
		Dialog.showInfo("getHeadPropertie", e.getLocalizedMessage());
	}

	if (propertyValue != null) {
	RecordValue rv = (RecordValue) propertyValue;
	EList<BasicPropertyAssociation> fields = rv.getOwnedFieldValues();

		pa = GetProperties.getRecordField(fields, "Company_Name");
		if (pa != null) {
			head.Company_Name = ((StringLiteral) pa.getValue()).getValue();
		}
		pa = GetProperties.getRecordField(fields, "Engineering_Location");
		if (pa != null) {
			head.Engineering_Location = ((StringLiteral) pa.getValue()).getValue();
		}
		pa = GetProperties.getRecordField(fields, "Customer_Name");
		if (pa != null) {
			head.Customer_Name = ((StringLiteral) pa.getValue()).getValue();
		}
		pa = GetProperties.getRecordField(fields, "Model_Year_Program");
		if (pa != null) {
			head.Model_Year_Program = ((StringLiteral) pa.getValue()).getValue();
		}
		pa = GetProperties.getRecordField(fields, "Subject");
		if (pa != null) {
			head.Subject = ((StringLiteral) pa.getValue()).getValue();
		}
		pa = GetProperties.getRecordField(fields, "DFMEA_Start_Data");
		if (pa != null) {
			head.DFMEA_Start_Data = ((StringLiteral) pa.getValue()).getValue();
		}
		pa = GetProperties.getRecordField(fields, "DFMEA_Revision_Data");
		if (pa != null) {
			head.DFMEA_Revision_Data = ((StringLiteral) pa.getValue()).getValue();
		}
		pa = GetProperties.getRecordField(fields, "Cross_Func_Team");
		if (pa != null) {
			head.Cross_Func_Team = ((StringLiteral) pa.getValue()).getValue();
		}
		pa = GetProperties.getRecordField(fields, "DFMEA_ID");
		if (pa != null) {
			head.DFMEA_ID = ((StringLiteral) pa.getValue()).getValue();
		}
		pa = GetProperties.getRecordField(fields, "Design_Responsibility");
		if (pa != null) {
			head.Design_Responsibility = ((StringLiteral) pa.getValue()).getValue();
		}
		pa = GetProperties.getRecordField(fields, "Confidentiality_Level");
		if (pa != null) {
			head.Confidentiality_Level = ((StringLiteral) pa.getValue()).getValue();
		}
		pa = GetProperties.getRecordField(fields, "Focus_Component");
		if (pa != null) {
			head.Focus_component_name = ((StringLiteral) pa.getValue()).getValue();
		}
	}
	return head;
	}
}
