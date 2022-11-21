package org.osate.iso26262.fmea;

import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.AbstractNamedValue;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.EnumerationLiteral;
import org.osate.aadl2.IntegerLiteral;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.NamedValue;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RealLiteral;
import org.osate.aadl2.StringLiteral;
import org.osate.aadl2.errormodel.FaultTree.Event;
import org.osate.aadl2.errormodel.FaultTree.FaultTree;
import org.osate.aadl2.errormodel.FaultTree.FaultTreeType;
import org.osate.aadl2.errormodel.PropagationGraph.PropagationGraph;
import org.osate.aadl2.errormodel.PropagationGraph.util.Util;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.iso26262.fmea.fixfta.FTAGenerator;
import org.osate.ui.dialogs.Dialog;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeSet;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeToken;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;
import org.osate.xtext.aadl2.properties.util.GetProperties;

public class FmeaBuilder {

	public FmeaHead head;
	public Structure root_component;
	public Structure focus_component;

	public void Construct_structure_tree(ComponentInstance ci) {
		if(ci==null)
		{
			System.out.println("Construct_structure_tree ::  ci=null");
			return;
		}

		root_component = new Structure(null, ci);
		travel_structure_tree(root_component);


	}

	public void travel_structure_tree(Structure root_component) {
		if (root_component == null) {
			System.out.println("travel_instance ::   root_component=null");
			return;
		}


		for (ComponentInstance cc : root_component.ci.getComponentInstances()) {
			Structure t = new Structure(root_component, cc);
			root_component.low_level_components_map.put(t.getName(), t);
			travel_structure_tree(t);
		}
	}

	public void Print_Structure(Structure root, String indent) {
		root.Print(indent);
		for (Structure cc : root.low_level_components_map.values()) {
			Print_Structure(cc, indent + "\t\t");
		}
	}

	public void BuildFailureAndFuncNet(Structure root) {
		Collection<ErrorBehaviorState> states = EMV2Util.getAllErrorBehaviorStates(root.ci);
		PropagationGraph currentPropagationGraph = Util.generatePropagationGraph(root.ci.getSystemInstance(), false);
		for (ErrorBehaviorState si : states) {
			FTAGenerator generator = new FTAGenerator(currentPropagationGraph);
			FaultTree ftamodel = generator.getftaModel(root.ci, si, null, FaultTreeType.COMPOSITE_PARTS);
			root.LinkFTAevent(ftamodel.getRoot(), root.failure_modes.get(si.getName()));
		}
	}




	public void TravelFTARootEvent(Event event, String indent) {
		InstanceObject io = (InstanceObject) event.getRelatedInstanceObject();
		EObject nne = event.getRelatedEMV2Object();

		TypeToken type = (TypeToken) event.getRelatedErrorType();
		System.out.println(indent + "Event Name			:::::::" + event.getName());
		System.out.println(indent + "Sub event Logic			:::::::" + event.getSubEventLogic());
		System.out.println(indent + "RelatedInstanceObject 		:::::::" + io.getName());
		System.out.println(indent + "ComponentInstancePath 		:::::::" + io.getComponentInstancePath());
		if (nne instanceof NamedElement) {
			System.out.println(indent + "RelatedEMV2Object		:::::::" + EMV2Util.getPrintName((NamedElement) nne));
		}
		System.out.println(indent + "RelatedErrorType		:::::::" + EMV2Util.getPrintName(type));
		System.out.println(indent + "Probability 			:::::::" + event.getProbability());

		for (Event ei : event.getSubEvents()) {
			TravelFTARootEvent(ei, indent + "\t");
		}
	}

	public void FillAP(Structure root) {
		for (FailureMode fi : root.failure_modes.values()) {
			fi.SearchMaxrefS();
			fi.Cal_AP();
		}
		for (Structure ci : root.low_level_components_map.values()) {
			FillAP(ci);
		}
	}

	public static AP CalculateAp(Integer S, Integer O, Integer D) {
		if(!(InRange(S,1,10)&&InRange(O,1,10)&&InRange(D,1,10)))
		{
			Dialog.showInfo("Fill_failure_modes", "Range Error::  S-"+S+"  O-"+O+" D-"+D);
			return null;
		}
		if (O == 1 || S == 1) {
			return AP.Low;
		}
		if (InRange(S, 9, 10)) {
			if (InRange(O, 4, 5)) {
				if (D == 1) {
					return AP.Middle;
				}
			}
			if (InRange(O, 2, 3)) {
				if (InRange(D, 5, 6)) {
					return AP.Middle;
				}
				if (InRange(D, 1, 4)) {
					return AP.Low;
				}
			}
			return AP.High;
		}
		if (InRange(S, 7, 8)) {
			if(InRange(O, 8, 10)) {
				return AP.High;
			}
			if (InRange(O, 6, 7)) {
				if (InRange(D, 2, 10)) {
					return AP.High;
				} else {
					return AP.Middle;
				}
			}
			if (InRange(O, 4, 5)) {
				if (InRange(D, 7, 10)) {
					return AP.High;
				} else {
					return AP.Middle;
				}
			}
			if (InRange(O, 2, 3)) {
				if (InRange(D, 5, 10)) {
					return AP.Middle;
				} else {
					return AP.Low;
				}
			}
		}

		if (InRange(S, 4, 6)) {
			if(InRange(O, 8, 10))
			{
				if(InRange(D, 5, 10)) {
					return AP.High;
				} else {
					return AP.Middle;
				}
			}
			if (InRange(O, 6, 7)) {
				if (InRange(D, 2, 10)) {
					return AP.Middle;
				}
			}
			if (InRange(O, 4, 5)) {
				if(InRange(D, 7, 10)) {
					return AP.Middle;
				}
			}
			return AP.Low;
		}

		if (InRange(S, 2, 3)) {
			if (InRange(O, 8, 10)) {
				if (InRange(D, 5, 10)) {
					return AP.Middle;
				}
			}
			return AP.Low;
		}
		Dialog.showInfo("Fill_failure_modes", "No judgment::  S-" + S + "  O-" + O + " D-" + D);
		return null;

	}

	public static boolean InRange(int current, int min, int max) {
		return Math.max(min, current) == Math.min(current, max);
	}

	public static String getRecordStringProperty(EList<BasicPropertyAssociation> fields, String recname) {
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, recname);
		String result = null;
		;
		if (xref != null) {
			PropertyExpression val = xref.getOwnedValue();
			result = ((StringLiteral) val).getValue();
		}
		return result;
	}

	public static String getRecordEnumerationProperty(EList<BasicPropertyAssociation> fields, String recname) {
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, recname);
		String result = null;
		;
		if (xref != null) {
			PropertyExpression val = xref.getOwnedValue();
			AbstractNamedValue eval = ((NamedValue) val).getNamedValue();
			result = ((EnumerationLiteral) eval).getName();
		}
		return result;
	}

	public static Double getRecordRealProperty(EList<BasicPropertyAssociation> fields, String recname) {
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, recname);
		Double result = null;
		;
		if (xref != null) {
			PropertyExpression val = xref.getOwnedValue();
			result = ((RealLiteral) val).getValue();
		}
		return result;
	}

	public static Integer getRecordIntProperty(EList<BasicPropertyAssociation> fields, String recname) {
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, recname);
		Integer result = null;
		;
		if (xref != null) {
			PropertyExpression val = xref.getOwnedValue();
			result = (int) ((IntegerLiteral) val).getValue();
		}
		return result;
	}

	public static Double getRecordUnitProperty(EList<BasicPropertyAssociation> fields, String fieldName,
			String unit) {
		PropertyExpression val = null;
		Double result = null;
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, fieldName);
		if (xref != null) {
			val = xref.getOwnedValue();

		}
		if (val instanceof IntegerLiteral) {
			result = ((IntegerLiteral) val).getScaledValue(unit);
		}
		return result;

	}

	public static void Dialog(String s1, String s2) {
		Dialog.showInfo(s1, s2);
	}

	public static String TypeSetName(TypeSet ts) {
		String result = "";
		if (ts != null) {
			result = "{" + ts.getFullName() + "}";
		}
		return result;
	}

	public void getHead() {
//		focus_component = root_component.Findstructure(focus_component_name);
//		head = focus_component.getHeadPropertie();

		head = root_component.getHeadPropertie();
		focus_component = root_component.Findstructure(head.Focus_component_name);
	}


}