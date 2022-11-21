package org.osate.iso26262.fmea;

import java.util.HashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.Property;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.errormodel.FaultTree.Event;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.properties.PropertyNotPresentException;
import org.osate.ui.dialogs.Dialog;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
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
		FillFunctionsAndFailuremode();
	}

	public void Print(String indent) {
		System.out.print(indent + "Component :: " + ci.getName());
		if (high_level_component != null) {
			System.out.print("  | (" + high_level_component.getName() + ")<<---");
		}
		if (low_level_components_map.size() > 0) {
			System.out.print("  |--->>(");
		for (String key : low_level_components_map.keySet()) {
			System.out.print(low_level_components_map.get(key).getName() + ",");
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

	public void FillFunctionsAndFailuremode() {

		for (ErrorBehaviorState es : EMV2Util.getAllErrorBehaviorStates(ci)) {

			FailureMode fmi = new FailureMode(ci, es, es.getTypeSet());
			Function fi = new Function(ci, es, es.getTypeSet());
			fmi.ref_component = this;
			fi.ref_component = this;
			failure_modes.put(fmi.id, fmi);
			Function reff = null;
			if (functions.containsKey(fi.funcname)) {
				reff = Function.Check_Merge(functions.get(fi.funcname), fi);
				reff.ref_fail_modes.add(fmi);
				functions.put(fi.funcname, reff);
			} else {
				functions.put(fi.funcname, fi);
				reff = fi;
				fi.ref_fail_modes.add(fmi);
				fi.id = "f" + functions.size();
			}
			fmi.ref_func = reff;

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

									if (!fm.ref_func.func_cause.contains(tfm.ref_func)) {
										fm.ref_func.func_cause.add(tfm.ref_func);
										tfm.ref_func.func_effect.add(fm.ref_func);
									}

									System.out.println("add Error link::  " + fm.ref_component.getName() + "." + fm.id
											+ " <==> " + tfm.ref_component.getName() + "." + tfm.id);
								}
							} else {
								Dialog.showInfo("LinkFTAevent",
										"Component \"" + linkc.getName() + "\" don't have failuremode::"
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

	public String getName() {
		return ci.getName();
	}

	public Structure Findstructure(String name) {
		Structure result = null;
		boolean same = this.getName().equals(name);
//		System.out.println(
//				"Search:::" + (same ? "same" : "notsame") + "  \"" + this.getName()() + "\"--\"" + name + "\"");
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

		property = GetProperties.lookupPropertyDefinition(ci, "ISO26262", "FmeaHead");
		try {
			propertyValue = ci.getSimplePropertyValue(property);
		} catch (PropertyNotPresentException e) {
			propertyValue = null;
//			Dialog.showInfo("getHeadPropertie", e.getLocalizedMessage());
		}

		if (propertyValue != null) {
			RecordValue rv = (RecordValue) propertyValue;
			EList<BasicPropertyAssociation> fields = rv.getOwnedFieldValues();

			head.Company_Name = FmeaBuilder.getRecordStringProperty(fields, "CompanyName");

			head.Engineering_Location = FmeaBuilder.getRecordStringProperty(fields, "EngineeringLocation");

			head.Customer_Name = FmeaBuilder.getRecordStringProperty(fields, "CustomerName");

			head.Model_Year_Program = FmeaBuilder.getRecordStringProperty(fields, "ModelYearProgram");

			head.Subject = FmeaBuilder.getRecordStringProperty(fields, "Subject");

			head.DFMEA_Start_Data = FmeaBuilder.getRecordStringProperty(fields, "DFMEAStartData");

			head.DFMEA_Revision_Data = FmeaBuilder.getRecordStringProperty(fields, "DFMEARevisionData");

			head.Cross_Func_Team = FmeaBuilder.getRecordStringProperty(fields, "CrossFuncTeam");

			head.DFMEA_ID = FmeaBuilder.getRecordStringProperty(fields, "DFMEAID");

			head.Design_Responsibility = FmeaBuilder.getRecordStringProperty(fields, "DesignResponsibility");

			head.Confidentiality_Level = FmeaBuilder.getRecordStringProperty(fields, "ConfidentialityLevel");

			head.Focus_component_name = FmeaBuilder.getRecordStringProperty(fields, "FocusComponent");

		}
		if (head.Focus_component_name == null) {
			head.Focus_component_name = this.getName();
		}
		return head;
	}
}
