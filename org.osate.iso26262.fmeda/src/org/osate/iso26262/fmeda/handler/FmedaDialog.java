package org.osate.iso26262.fmeda.handler;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class FmedaDialog extends TitleAreaDialog {

	private String target;

	private String safetyGoal;
	private List<String> safetyGoals;
	private Combo safetyGoalCombo;

	private String ASILLevel;
	private String[] ASILLevels = { "D", "C", "B", "A" };
	private Combo ASILLevelCombo;

	private Button useFmeaBox;
	private Boolean useFmea = true;

	private Button csvExportBox;
	private Boolean csvExport = false;

	public FmedaDialog(Shell parentShell) {
		super(parentShell);
	}

	public void setSafetyGoals(List<String> sg) {
		safetyGoals = sg;
	}

	public void setTarget(String targetname) {
		target = targetname;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Fmeda Analysis" + (target.isEmpty() ? "" : " for " + target));
		setMessage("Select safety goal and ASIL level for system or component fmeda analysis.", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		Composite comboContainer = new Composite(area, SWT.NONE);
		GridLayout comboLayout = new GridLayout(2, false);
		comboContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comboContainer.setLayout(comboLayout);

		Label labelSafetyGoal = new Label(comboContainer, SWT.NONE);
		labelSafetyGoal.setText("Select Safety Goal :   ");

		safetyGoalCombo = new Combo(comboContainer, SWT.READ_ONLY | SWT.BORDER);
		String val[] = new String[safetyGoals.size()];
		for (int i = 0; i < safetyGoals.size(); i++) {
			val[i] = safetyGoals.get(i);
		}
		safetyGoalCombo.setItems(val);
		safetyGoalCombo.select(0);

		Label labelASILLevel = new Label(comboContainer, SWT.NONE);
		labelASILLevel.setText("Select ASIL Level :   ");

		ASILLevelCombo = new Combo(comboContainer, SWT.READ_ONLY | SWT.BORDER);
		ASILLevelCombo.setItems(ASILLevels);
		ASILLevelCombo.select(0);

		Composite buttonContainer = new Composite(area, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(1, false);
		buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonContainer.setLayout(buttonLayout);

		useFmeaBox = new Button(buttonContainer, SWT.CHECK);
		useFmeaBox.setText("Do analysis with FMEA (Failure Mode and Effect Analysis)");
		useFmeaBox.setSelection(useFmea);

		csvExportBox = new Button(buttonContainer, SWT.CHECK);
		csvExportBox.setText("Export CSV report (default Excel)");
		csvExportBox.setSelection(csvExport);

		return area;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		safetyGoal = safetyGoalCombo.getText();
		ASILLevel = ASILLevelCombo.getText();
		useFmea = useFmeaBox.getSelection();
		csvExport = csvExportBox.getSelection();
		super.okPressed();
	}

	public String getSafetyGoal() {
		return safetyGoal;
	}

	public String getASILLevel() {
		return ASILLevel;
	}

	public Boolean getUseFmea() {
		return useFmea;
	}

	public Boolean getCsvExport() {
		return csvExport;
	}

}
