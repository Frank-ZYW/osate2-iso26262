package org.osate.iso26262.fmeda.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.EObjectURIWrapper;
import org.osate.aadl2.modelsupport.resources.OsateResourceUtil;
import org.osate.iso26262.fmea.FMEDAPI;
import org.osate.iso26262.fmeda.FmedaFaultMode;
import org.osate.iso26262.fmeda.FmedaProperty;
import org.osate.iso26262.fmeda.FmedaTable;
import org.osate.iso26262.fmeda.report.FmedaReportGenerator;
import org.osate.iso26262.fmeda.util.PropertyParseUtil;
import org.osate.ui.dialogs.Dialog;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;

import jxl.write.WriteException;

public class FmedaHandler extends AbstractHandler {

	private static String SAFETY_GOAL = null;
	private static List<String> safetyGoals = null;
	private static String ASIL_LEVEL = null;
	private static Boolean USE_FMEA = null;
	private static Boolean CSV_EXPORT = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		/** get target instance object **/

		InstanceObject object = getTarget(HandlerUtil.getCurrentSelection(event));
		if (object == null) {
			Dialog.showWarning("Failure Modes Effects and Diagnostic Analysis", "Please choose an instance model!");
			return IStatus.ERROR;
		}
		ComponentInstance target = object instanceof ComponentInstance ? (ComponentInstance) object : object.getSystemInstance();

		////////////////////////////////////

		FMEDAPI api = new FMEDAPI(target);
		safetyGoals = api.Get_Safety_Goal();

		/** empty safety goal error **/

		if (safetyGoals.isEmpty()) {
			Dialog.showWarning("Failure Modes Effects and Diagnostic Analysis", "Target component must has at lease one safety goal!");
			return IStatus.ERROR;
		}

		/** choose safety goal & ASIL level & report type & analysis type **/

		final Display fmedaDialog = PlatformUI.getWorkbench().getDisplay();
		fmedaDialog.syncExec(() -> {
			IWorkbenchWindow window;
			Shell sh;

			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			sh = window.getShell();

			FmedaDialog diag = new FmedaDialog(sh);
			diag.setTarget("'" + (target instanceof SystemInstance ? target.getName() : target.getComponentInstancePath()) + "'");
			diag.setSafetyGoals(safetyGoals);
			diag.open();
			SAFETY_GOAL = diag.getSafetyGoal();
			ASIL_LEVEL = diag.getASILLevel();
			USE_FMEA = diag.getUseFmea();
			CSV_EXPORT = diag.getCsvExport();
		});
		// choose cancel button
		if (SAFETY_GOAL == null) {
			return IStatus.ERROR;
		}

		////////////////////////////////////

		/** get safety-related component **/

		List<ComponentInstance> ciList = new ArrayList<ComponentInstance>();

		if (USE_FMEA) {
			ciList = api.Get_Calcul_Instance(target, SAFETY_GOAL);
		} else {
			ciList = EcoreUtil2.getAllContentsOfType(target, ComponentInstance.class);
			// if no sub-components then add itself
			if (ciList.isEmpty()) {
				ciList.add(target.getComponentInstance());
			}
		}

		FmedaTable fmedaTb = new FmedaTable();
		fmedaTb.blockName = target.getFullName();
		fmedaTb.safetyGoal = SAFETY_GOAL;
		fmedaTb.ASIL = ASIL_LEVEL;

		/** check the legality of FmedaProperty **/

		List<ComponentInstance> illegalCiList = new ArrayList<ComponentInstance>();

		for (ComponentInstance ci : ciList) {
			FmedaProperty fp = getFmedaPropertyFromComponent(ci);
			if (fp != null && fp.isLegal()) {
				fmedaTb.addFmedaProperty(fp);
			} else {
				if (USE_FMEA) {
					illegalCiList.add(ci);
				} else if (fp != null) {
					illegalCiList.add(ci);
				}
			}
		}

		////////////////////////////////////

		/** illegal FmedaProperty error **/

		if (!illegalCiList.isEmpty()) {
			final Display errorDialog = PlatformUI.getWorkbench().getDisplay();
			errorDialog.syncExec(() -> {
				IWorkbenchWindow window;
				Shell sh;

				window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				sh = window.getShell();

				ErrorDialog diag = new ErrorDialog(sh, illegalCiList);
				diag.open();
			});
			return IStatus.ERROR;
		}

		/** empty table warning **/

		if (fmedaTb.isEmpty()) {
			Dialog.showInfo("Failure Modes Effects and Diagnostic Analysis", "No safety-related component under this safety goal!");
			return Status.OK_STATUS;
		}

		////////////////////////////////////

		// do calculation
		fmedaTb.fmedaCalc();

		// write report
		try {
			FmedaReportGenerator reportGen = new FmedaReportGenerator();
			reportGen.setFmedaTable(fmedaTb);
			if (!CSV_EXPORT) {
				reportGen.writeExcelReport(target);
			} else {
				reportGen.writeCsvReport(target);
			}
		} catch (WriteException | IOException | CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Dialog.showError("Failure Modes Effects and Diagnostic Analysis", "Report generate Error!");
			return IStatus.ERROR;
		}
		Dialog.showInfo("Failure Modes Effects and Diagnostic Analysis", "FMEDA Analyise Complete!");

		return Status.OK_STATUS;
	}

	/**
	 * Get target instance object
	**/
	private InstanceObject getTarget(ISelection currentSelection) {
		if (currentSelection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) currentSelection;
			if (iss.size() == 1) {
				Object obj = iss.getFirstElement();
				if (obj instanceof InstanceObject) {
					return (InstanceObject) obj;
				}
				if (obj instanceof EObjectURIWrapper) {
					EObject eObject = new ResourceSetImpl().getEObject(((EObjectURIWrapper) obj).getUri(), true);
					if (eObject instanceof InstanceObject) {
						return (InstanceObject) eObject;
					}
				}
				if (obj instanceof IFile) {
					URI uri = OsateResourceUtil.toResourceURI((IFile) obj);
					Resource res = new ResourceSetImpl().getResource(uri, true);
					EList<EObject> rl = res.getContents();
					if (!rl.isEmpty()) {
						return (InstanceObject) rl.get(0);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get FMEDA property from Component
	**/
	private FmedaProperty getFmedaPropertyFromComponent(ComponentInstance ci) {
		FmedaProperty fp = null;

		for (ErrorBehaviorState state : EMV2Util.getAllErrorBehaviorStates(ci)) {
			if (state.getFullName().equals("FmedaState")) {

				/** get safety properties **/

				EList<BasicPropertyAssociation> spField = PropertyParseUtil.getPropertyField("ISO26262::FMEDASafetyProperties",
						ci, state, state.getTypeSet());

				if (spField != null) {
					fp = new FmedaProperty();

					fp.componentName = ci.getFullName();
					fp.failureRate = PropertyParseUtil.getDoubleProperty(spField, "failurerate", 0.0);
					fp.isSafetyRelated = PropertyParseUtil.getBooleanProperty(spField, "safetyrelated", false);

					/** get failure modes **/

					for (ErrorBehaviorState state_ : EMV2Util.getAllErrorBehaviorStates(ci)) {

						EList<BasicPropertyAssociation> fmField = PropertyParseUtil
								.getPropertyField("ISO26262::FailureMode", ci, state_, state_.getTypeSet());

						if (fmField != null) {
							FmedaFaultMode fm = new FmedaFaultMode();

							fm.modeName = PropertyParseUtil.getStringProperty(fmField, "modename", state_.getFullName());
							fm.distribution = PropertyParseUtil.getDoubleProperty(fmField, "distribution", 0.0);
							fm.hasSPF = PropertyParseUtil.getBooleanProperty(fmField, "violate_sp_satety", false);
							fm.spf_SM = PropertyParseUtil.getStringProperty(fmField, "spf_sm", "none");
							fm.spf_DC = PropertyParseUtil.getDoubleProperty(fmField, "spf_dc", 0.0);
							fm.hasMPF = PropertyParseUtil.getBooleanProperty(fmField, "violate_mp_satety", false);
							fm.mpf_SM = PropertyParseUtil.getStringProperty(fmField, "mpf_sm", "none");
							fm.mpf_DC = PropertyParseUtil.getDoubleProperty(fmField, "mpf_dc", 0.0);

							fp.addFmedaFaultMode(fm);
						}
					}
				}
			}
		}
		return fp;
	}

}
