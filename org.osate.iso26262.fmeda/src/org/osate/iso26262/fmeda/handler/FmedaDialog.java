package org.osate.iso26262.fmeda.handler;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
	private String[] ASILLevels = { "A", "B", "C", "D" };
	private Combo ASILLevelCombo;

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
		Composite container = new Composite(area, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);

		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);

		Label labelSafetyGoal = new Label(container, SWT.NONE);
		labelSafetyGoal.setText("Select Safety Goal :   ");

		safetyGoalCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		String val[] = new String[safetyGoals.size()];
		for (int i = 0; i < safetyGoals.size(); i++) {
			val[i] = safetyGoals.get(i);
		}
		safetyGoalCombo.setItems(val);
		safetyGoalCombo.select(0);

		Label labelASILLevel = new Label(container, SWT.NONE);
		labelASILLevel.setText("Select ASIL Level :   ");

		ASILLevelCombo = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		ASILLevelCombo.setItems(ASILLevels);
		ASILLevelCombo.select(3);

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
		super.okPressed();
	}

	public String getSafetyGoal() {
		return safetyGoal;
	}

	public String getASILLevel() {
		return ASILLevel;
	}

}
