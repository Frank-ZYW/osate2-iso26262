package org.osate.iso26262.fmeda.handler;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.osate.aadl2.instance.ComponentInstance;

public class ErrorDialog extends Dialog {

	private final Model model;

	public ErrorDialog(final Shell shell, final List<ComponentInstance> illegalComponentList) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.model = new Model(illegalComponentList);
	}

	private static final class Model {

		private final String message;
		private final Integer[] elements;
		private final String[] ciNames;
		private final String[] instanceObjectPaths;

		public Model(final List<ComponentInstance> illegalComponentList) {
			final int size = illegalComponentList.size();

			elements = new Integer[size];
			for (int i = 0; i < size; i++) {
				elements[i] = Integer.valueOf(i);
			}

			ciNames = new String[size];
			instanceObjectPaths = new String[size];

			int i = 0;
			for (ComponentInstance ci : illegalComponentList) {
				ciNames[i] = ci.getFullName();
				instanceObjectPaths[i] = ci.getInstanceObjectPath();
				i += 1;
			}

			message = "The following Components must have legal FMEDA properties!";
		}

		public String getMessage() {
			return message;
		}

		public Integer[] getElements() {
			return elements;
		}

		public String getCiName(final int idx) {
			return ciNames[idx];
		}

		public String getInstanceObjectPath(final int idx) {
			return instanceObjectPaths[idx];
		}

	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		shell.setText("Fmeda Analysis Error");
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);

		/* Message on top */
		final Label label = new Label(composite, SWT.NONE);
		label.setText(model.getMessage());
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

		final TableViewer viewer = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setText("Component Name");
		col1.getColumn().setWidth(300);
		col1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return model.getCiName((Integer) element);
			}
		});

		final TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setText("Instance Object Path");
		col2.getColumn().setWidth(450);
		col2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return model.getInstanceObjectPath((Integer) element);
			}
		});

		final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getControl().setLayoutData(layoutData);
		viewer.setContentProvider(new ModelContentProvider());
		viewer.setInput(model);

		return composite;
	}

	private static final class ModelContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(final Object inputElement) {
			return ((Model) inputElement).getElements();
		}
	}

}
