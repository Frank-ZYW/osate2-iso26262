package org.osate.iso26262.hazard;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.EcoreUtil2;
import org.osate.aadl2.AbstractNamedValue;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.Element;
import org.osate.aadl2.EnumerationLiteral;
import org.osate.aadl2.IntegerLiteral;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.ModalPropertyValue;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.NamedValue;
import org.osate.aadl2.PropertyAssociation;
import org.osate.aadl2.PropertyConstant;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.StringLiteral;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.ConnectionInstance;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.xtext.aadl2.errormodel.errorModel.ConditionElement;
import org.osate.xtext.aadl2.errormodel.errorModel.ConditionExpression;
import org.osate.xtext.aadl2.errormodel.errorModel.EMV2Path;
import org.osate.xtext.aadl2.errormodel.errorModel.EMV2PropertyAssociation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorTransition;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorEvent;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorSource;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorTypes;
import org.osate.xtext.aadl2.errormodel.errorModel.EventOrPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeSet;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeToken;
import org.osate.xtext.aadl2.errormodel.util.EMV2Properties;
import org.osate.xtext.aadl2.errormodel.util.EMV2TypeSetUtil;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;
import org.osate.xtext.aadl2.properties.util.GetProperties;

public class HARAReport {


	public void doHARAReport(SystemInstance si) {
		FileExport report = new FileExport("HARA", si);


		report.prepare();

		reportHeading(report);

		List<ComponentInstance> cilist = EcoreUtil2.getAllContentsOfType(si, ComponentInstance.class);
		processHazards(si, report);

		for (ConnectionInstance conni : si.getConnectionInstances()) {
			processHazards(conni, report);
		}

		for (ComponentInstance componentInstance : cilist) {

			processHazards(componentInstance, report);

			for (ConnectionInstance conni : componentInstance.getConnectionInstances()) {
				processHazards(conni, report);
			}

		}
		report.saveToFile();
	}

	protected void processHazards(ComponentInstance ci, FileExport report) {

		int rows = 0;
		for (ErrorBehaviorTransition trans : EMV2Util.getAllErrorBehaviorTransitions(ci.getComponentClassifier())) {
			ConditionExpression cond = trans.getCondition();
			if (cond instanceof ConditionElement) {
				ConditionElement condElement = (ConditionElement) trans.getCondition();
				EventOrPropagation eop = EMV2Util.getErrorEventOrPropagation(condElement);
				if (eop instanceof ErrorEvent) {
					ErrorEvent errorEvent = (ErrorEvent) eop;
					List<EMV2PropertyAssociation> PA = getISO26262HazardsProperty(ci, errorEvent,
							errorEvent.getTypeSet());

					rows += reportHazardProperty(ci, PA, errorEvent, errorEvent.getTypeSet(), errorEvent, report);
				}
				// condElement.getIncoming()
			}

		}

		for (ErrorBehaviorState state : EMV2Util.getAllErrorBehaviorStates(ci)) {

			List<EMV2PropertyAssociation> PA = getISO26262HazardsProperty(ci, state, state.getTypeSet());
			rows += reportHazardProperty(ci, PA, state, state.getTypeSet(), state, report);
		}

		// report all error sources as hazards if they have the property
		Collection<ErrorSource> eslist = EMV2Util.getAllErrorSources(ci.getComponentClassifier());
		Collection<ErrorPropagation> oeplist = EMV2Util.getAllOutgoingErrorPropagations(ci.getComponentClassifier());

		for (ErrorSource errorSource : eslist) {
			NamedElement ne = errorSource.getSourceModelElement();
			ErrorBehaviorState failureMode = errorSource.getFailureModeReference();

			List<EMV2PropertyAssociation> HazardPA = Collections.emptyList();
			TypeSet ts = null;
			NamedElement target = null;
			Element localContext = null;
			// not dealing with type set as failure mode
			if (failureMode != null) {
				// state is originating hazard, possibly with a type set
				ts = failureMode.getTypeSet();
				// error source a local context
				HazardPA = getISO26262HazardsProperty(ci, failureMode, ts);
				target = failureMode;
				localContext = errorSource;
			}
			if (HazardPA.isEmpty()) {
				// error source is originating hazard
				ts = errorSource.getTypeTokenConstraint();
				if (ts == null && ne instanceof ErrorPropagation) {
					ts = ((ErrorPropagation) ne).getTypeSet();
				}
				HazardPA = getISO26262HazardsProperty(ci, errorSource, ts);
				target = errorSource;
				localContext = null;
				if (HazardPA.isEmpty() && errorSource.getFailureModeType() != null) {
					ts = errorSource.getFailureModeType();
					HazardPA = getISO26262HazardsProperty(ci, errorSource, ts);
				}
			}
			if (!HazardPA.isEmpty()) {
				rows += reportHazardProperty(ci, HazardPA, target, ts, localContext, report);
			}
		}

		for (ErrorPropagation ep : oeplist) {
			TypeSet ts = null;
			NamedElement target = null;
			Element localContext = null;
			// error propagation is originating hazard
			ts = ep.getTypeSet();

			List<EMV2PropertyAssociation> HazardPA = getISO26262HazardsProperty(ci, ep, ts);
			target = ep;
			localContext = null;

			if (!HazardPA.isEmpty()) {
				rows += reportHazardProperty(ci, HazardPA, target, ts, localContext, report);
			}
		}
		report.mergecell(0, report.currow - rows, 0, report.currow - 1);
	}

	protected void processHazards(ConnectionInstance conni, FileExport report) {
		ErrorSource ces = EMV2Util.findConnectionErrorSourceForConnection(conni);
		if (ces == null) {
			return;
		}
		Element localContext = null;
		// error propagation is originating hazard
		TypeSet ts = ces.getTypeTokenConstraint();
		List<EMV2PropertyAssociation> HazardPA = getISO26262HazardsProperty(conni, ces, ts);
		NamedElement target = ces;

		if (!HazardPA.isEmpty()) {
			int rows = reportHazardProperty(conni, HazardPA, target, ts, localContext, report);
			report.mergecell(0, report.currow - rows, 0, report.currow - 1);
		}
	}

	protected String getEnumerationorIntegerPropertyValue(PropertyAssociation pa) {
		if (pa == null) {
			return "";
		}
		for (ModalPropertyValue modalPropertyValue : pa.getOwnedValues()) {
			PropertyExpression val = modalPropertyValue.getOwnedValue();
			if (val instanceof NamedValue) {
				AbstractNamedValue eval = ((NamedValue) val).getNamedValue();
				if (eval instanceof EnumerationLiteral) {
					return ((EnumerationLiteral) eval).getName();

				} else if (eval instanceof PropertyConstant) {
					return ((PropertyConstant) eval).getName();
				}
			} else if (val instanceof IntegerLiteral) {
				// empty string to force integer conversion to string
				return "" + ((IntegerLiteral) val).getValue();
			}
		}

		return "";
	}

	protected int reportHazardProperty(InstanceObject ci, List<EMV2PropertyAssociation> PAList, NamedElement target,
			TypeSet ts, Element localContext, FileExport report) {
		int rows = 0;
		int absrows = 0;
		String targetName;
		if (PAList.isEmpty()) {
			return rows;
		}

		if (target == null) {
			targetName = "";
		} else {
			targetName = EMV2Util.getPrintName(target);
			if (target instanceof ErrorEvent) {
				targetName = "event " + targetName;
			}

			if (target instanceof ErrorBehaviorState) {
				targetName = "state " + targetName;
			}
		}



		for (EMV2PropertyAssociation PA : PAList) {

			for (ModalPropertyValue modalPropertyValue : PA.getOwnedValues()) {

				PropertyExpression peVal = modalPropertyValue.getOwnedValue();
				ListValue lv = (ListValue) peVal;
				if (reportEnumerationOrIntegerPropertyConstantPropertyValue(
						((RecordValue) lv.getOwnedListElements().get(0)).getOwnedFieldValues(), "SafetyCategory", null)
						.equals("SG")) {
				for (PropertyExpression pe : lv.getOwnedListElements()) {
					EList<BasicPropertyAssociation> fields = ((RecordValue) pe).getOwnedFieldValues();

					// for all error types/aliases in type set or the element identified in the containment clause
//				EList<BasicPropertyAssociation> fields = ((RecordValue) modalPropertyValue.getOwnedValue()).getOwnedFieldValues();
					if (ts != null) {
						// do smaller of ts or hazard type set.
						EList<EMV2Path> epathlist = PA.getEmv2Path();
						for (EMV2Path ep : epathlist) {
							ErrorTypes et = EMV2Util.getErrorType(ep);
							ErrorTypes targettype = ts;
							if (et != null && EMV2TypeSetUtil.contains(ts, et)) {
								targettype = et;
							}

							if (targettype instanceof TypeSet) {

								for (TypeToken token : ((TypeSet) targettype).getTypeTokens()) {
									reportHaraEntry(report, fields, ci, targetName, EMV2Util.getName(token));
									rows++;
								}
							} else {
								reportHaraEntry(report, fields, ci, targetName, EMV2Util.getName(targettype));
								rows++;
							}
						}
						absrows = 0;
					} else {
						// did not have a type set. Let's use fmr (state of type set as failure mode.
						if (localContext == null) {
							reportHaraEntry(report, fields, ci, targetName, "");
							rows++;
							absrows++;
						} else {
							reportHaraEntry(report, fields, ci, EMV2Util.getPrintName(localContext),
									EMV2Util.getPrintName(target));
							rows++;
							absrows++;
						}
					}
				}
			}
			}
		}
		report.mergecell(1, report.currow - absrows, 1, report.currow - 1);
		report.mergecell(2, report.currow - absrows, 2, report.currow - 1);
		return rows;
	}



	protected void reportHeading(FileExport report) {

		String[] Heads = new String[] { "Component", "Error Model Element", "HazardName", "Description", "Mishap",
				"Evironment", "VerificationMethod", "CrossReference", "Comment", "Probability", "SeverityComment",
				"Severity", "ControllabilityComment", "Controllability", "ExposureComment", "Exposure", "ASIL",
				"SafetyDescription", /* "SafetyCategory", */
				"OperatMode", "FTTI", "SafeState", /* "MissionTime" */ };
		for (String headtext : Heads) {
			report.addheadcell(headtext);
		}
		report.nextline();

	}

	protected void reportHaraEntry(FileExport report, EList<BasicPropertyAssociation> fields, InstanceObject ci,
			String failureModeName, String typetext) {
		reportHaraEntryISO26262(report, fields, ci, failureModeName, typetext);
	}

	protected void reportHaraEntryISO26262(FileExport report, EList<BasicPropertyAssociation> fields, InstanceObject ci,
			String failureModeName, String typetext) {

		/*
		 * We include the parent component name if not null and if this is not the root system
		 * instance.
		 */
//		if () {
			String Severity;
			String Controllability;
			String Exposure;
			String ASIL;

			String componentName = ci.getName();
			if ((ci.getContainingComponentInstance() != null)
					&& (ci.getContainingComponentInstance() != ci.getSystemInstance())) {
				componentName = ci.getContainingComponentInstance().getName() + "/" + componentName;
			}
			if (ci instanceof SystemInstance) {
				componentName = "Root system";
			}
			// component name & error propagation name/type
			report.addcell(componentName);
			report.addcell(" \"" + (typetext.isEmpty() ? "" : typetext)
					+ (failureModeName.isEmpty() ? "" : " on " + failureModeName) + "\"");

			reportStringProperty(fields, "HazardName", report);

			reportStringProperty(fields, "Description", report);

			reportStringProperty(fields, "Mishap", report);

			reportStringProperty(fields, "Evironment", report);

			reportStringProperty(fields, "VerificationMethod", report);

			reportStringProperty(fields, "CrossReference", report);

			reportStringProperty(fields, "Comment", report);

			reportEnumerationOrIntegerPropertyConstantPropertyValue(fields, "Probability", report);

			reportStringProperty(fields, "SeverityComment", report);

			Severity = reportEnumerationOrIntegerPropertyConstantPropertyValue(fields, "Severity", report);

			reportStringProperty(fields, "ControllabilityComment", report);

			Controllability = reportEnumerationOrIntegerPropertyConstantPropertyValue(fields, "Controllability",
					report);

			reportStringProperty(fields, "ExposureComment", report);

			Exposure = reportEnumerationOrIntegerPropertyConstantPropertyValue(fields, "Exposure", report);

			ASIL = reportEnumerationOrIntegerPropertyConstantPropertyValue(fields, "ASIL", null);

			reportASIL(Severity, Controllability, Exposure, ASIL, report);

			reportStringProperty(fields, "SafetyDescription", report);

//			reportEnumerationOrIntegerPropertyConstantPropertyValue(fields, "SafetyCategory", report);

			reportStringProperty(fields, "OperatMode", report);

			reportNumberUnitPropertyValue(fields, "FTTI", report, "ms");

			reportStringProperty(fields, "SafeState", report);

//			reportNumberUnitPropertyValue(fields, "MissionTime", report, "hr");

			report.nextline();
//		}
	}

	protected void reportASIL(String Severity, String Controllability, String Exposure, String ASIL,
			FileExport report)
	{
		String Cal_ASIL = CalCulateASIL(Severity, Controllability, Exposure);
		System.out.println(ASIL + " ---- " + Cal_ASIL);
		if (Cal_ASIL.equals(ASIL)) {
			report.addcell(ASIL);
		}
		else if (ASIL.equals(""))
		{
			report.addcell("{" + Cal_ASIL + "}");
		}
		else {
			report.addRedcell(ASIL + "{" + Cal_ASIL + "}");
		}
	}

	protected String CalCulateASIL(String Severity, String Controllability, String Exposure) {
		if(!Severity.equals("")&&!Controllability.equals("")&&!Exposure.equals(""))
		{
			int s = Integer.parseInt(Severity.substring(Severity.length() - 1));
			int c = Integer.parseInt(Controllability.substring(Controllability.length() - 1));
			int e = Integer.parseInt(Exposure.substring(Exposure.length() - 1));
			int asil = s + c + e;
			switch (asil) {
			case 10:
				return "D";
			case 9:
				return "C";
			case 8:
				return "B";
			case 7:
				return "A";
			default:
				return "QM";
			}
		} else {
			return "";
		}
	}

	protected void reportStringProperty(EList<BasicPropertyAssociation> fields, String fieldName,
			FileExport report) {
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, fieldName);
		String text = "";
		if (xref != null) {
			PropertyExpression val = xref.getOwnedValue();
			if (val instanceof StringLiteral) {
				text = ((StringLiteral) val).getValue();
			}
			if (val instanceof ListValue) {
				ListValue lv = (ListValue) val;
				text = "";
				for (PropertyExpression pe : lv.getOwnedListElements()) {
					if (text.length() > 0) {
						text += " or ";
					}
					text += stripQuotes(((StringLiteral) pe).getValue());
				}
			}
		}
		if (text != null) {
			text = "\"" + stripQuotes(text) + "\"";
		}
		report.addcell(text);

	}

	protected String stripQuotes(String text) {
		if (text.startsWith("\"") && text.endsWith("\"")) {
			return text.substring(1, text.length() - 1);
		}
		return text;
	}

	protected String reportEnumerationOrIntegerPropertyConstantPropertyValue(EList<BasicPropertyAssociation> fields,
			String fieldName, FileExport report) {
		// added code to handle integer value and use of property constant instead of enumeration literal
		String result;
		PropertyExpression val = null;
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, fieldName);
		if (xref != null) {
			val = xref.getOwnedValue();
		}
		result = EMV2Properties.getEnumerationOrIntegerPropertyConstantPropertyValue(val);
		if (report != null) {
			report.addcell(result);
		}
		return result;
	}


	protected void reportNumberUnitPropertyValue(EList<BasicPropertyAssociation> fields, String fieldName,
			FileExport report, String unit) {
		PropertyExpression val = null;
		String text = "";
		BasicPropertyAssociation xref = GetProperties.getRecordField(fields, fieldName);
		if (xref != null) {
			val = xref.getOwnedValue();

		}
		if (val instanceof IntegerLiteral) {
			text = "" + ((IntegerLiteral) val).getScaledValue(unit) + " " + unit;
		}
		report.addcell(text);
	}



	protected List<EMV2PropertyAssociation> getISO26262HazardsProperty(NamedElement ci, NamedElement target,
			TypeSet ts) {
		return EMV2Properties.getProperty("ISO26262::Hazards", ci, target, ts);
	}
}
