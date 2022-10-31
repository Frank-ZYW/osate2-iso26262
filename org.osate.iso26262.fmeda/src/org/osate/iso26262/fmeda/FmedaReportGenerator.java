package org.osate.iso26262.fmeda;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.iso26262.fmeda.util.ReportUtil;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class FmedaReportGenerator {

	public FmedaTable table;

	// cell format
	public WritableCellFormat titleCF;
	public WritableCellFormat headCF;
	public WritableCellFormat bodyCF;
	public WritableCellFormat resultCF;

	public FmedaReportGenerator() throws WriteException {
		this.table = null;
		this.titleCF = ReportUtil.getTitleCellFormat();
		this.headCF = ReportUtil.getHeadCellFormat();
		this.bodyCF = ReportUtil.getBodyCellFormat();
		this.resultCF = ReportUtil.getResultCellFormat();
	}

	/**
	 * Set FmedaTable
	**/
	public void setFmedaTable(FmedaTable table) {
		this.table = table;
	}

	/**
	 * Write report to hard disk
	 * @throws IOException, WriteException
	**/
	public void writeReport(EObject root) throws IOException, WriteException {
		// get report path
		IPath path = ReportUtil.getReportPath(root);
		AadlUtil.makeSureFoldersExist(path);

		// create report file
		IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		File file = ifile.getLocation().toFile();
		file.createNewFile();

		// create workbook & sheet
		WritableWorkbook workbook = Workbook.createWorkbook(file);
		WritableSheet sheet = workbook.createSheet("Sheet1", 0);

		// write report
		int currentRow = 0;
		currentRow = this.titleGenerate(sheet, currentRow);
		currentRow = this.headGenerate(sheet, currentRow);
		currentRow = this.bodyGenerate(sheet, currentRow);
		currentRow = this.resultGenerate(sheet, currentRow);

		// save report
		workbook.write();
		workbook.close();
	}

	/**
	 * Generate title
	 * @throws RowsExceededException, WriteException
	**/
	public int titleGenerate(WritableSheet sheet, int currentRow) throws RowsExceededException, WriteException {
		int currentColumn = 0;

		sheet.addCell(new Label(currentColumn++, currentRow, "Block Name", this.titleCF));
		sheet.addCell(new Label(currentColumn++, currentRow, this.table.blockName, this.titleCF));

		sheet.addCell(new Label(currentColumn++, currentRow, "Safety Goal", this.titleCF));
		sheet.addCell(new Label(currentColumn, currentRow, this.table.safetyGoal, this.titleCF));

		currentColumn += 8;
		sheet.addCell(new Label(currentColumn++, currentRow, "ASIL", this.titleCF));
		sheet.addCell(new Label(currentColumn, currentRow, this.table.ASIL, this.titleCF));

		// merge cells
		sheet.mergeCells(3, currentRow, 10, currentRow);
		// set row height
		sheet.setRowView(currentRow, 500);

		return ++currentRow;
	}

	/**
	 * Generate head
	 * @throws RowsExceededException, WriteException
	**/
	public int headGenerate(WritableSheet sheet, int currentRow) throws RowsExceededException, WriteException {
		int currentColumn = 0;

		List<String> head = new ArrayList<String>();
		head.add("Component Name");
		head.add("Failure rate/FIT");
		head.add("Safety-related component to be considered in the calculation?");
		head.add("Failure Mode");
		head.add("Failure mode distribution");
		head.add("Failure mode that has the potential to violate the safety goal in absence of safety mechanisms?");
		head.add("Safety mechanism(s) allowing to prevent the failure mode from violating the safety goal?");
		head.add("Failure mode coverage wrt. violation of safety goal");
		head.add("Residual or Single-Point Fault failure rate/FIT");
		head.add("Failure mode that may lead to the violation of safety goal in combination with an independent failure of another component?");
		head.add("Detection means? Safety mechanism(s) allowing to prevent the failure mode from being latent?");
		head.add("Failure mode coverage wrt. Latent failures,");
		head.add("Latent Multiple-Point Fault failure rate/FIT\n");

		for (String each : head) {
			// write cell & set column width
			sheet.addCell(new Label(currentColumn, currentRow, each, this.headCF));
			sheet.setColumnView(currentColumn++, head.indexOf(each) == 0 ? 28 : 18);
		}

		return ++currentRow;
	}

	/**
	 * Generate body
	 * @throws RowsExceededException, WriteException
	**/
	public int bodyGenerate(WritableSheet sheet, int currentRow) throws RowsExceededException, WriteException {
		int currentColumn = 0;

		for (FmedaProperty fp : this.table.fpList) {
			for (FmedaFaultMode fm : fp.faultModes) {

				List<String> body = new ArrayList<String>();
				body.add(fp.componentName);
				body.add(fp.failureRate.toString());
				body.add(fp.isSafetyRelated ? "YES" : "NO");
				body.add(fm.modeName);
				body.add(fm.distribution.toString() + "%");
				body.add(fm.hasSPF ? "X" : "");
				body.add(fm.hasSPF ? fm.spf_SM : "");
				body.add(fm.hasSPF ? fm.spf_DC.toString() + "%" : "");
				body.add(String.format("%.3f", fm.sprf));
				body.add(fm.hasMPF ? "X" : "");
				body.add(fm.hasMPF ? fm.mpf_SM : "");
				body.add(fm.hasMPF ? fm.mpf_DC.toString() + "%" : "");
				body.add(String.format("%.3f", fm.mpfl));

				for (String each : body) {
					sheet.addCell(new Label(currentColumn++, currentRow, each, this.bodyCF));
				}

				// set row height
				sheet.setRowView(currentRow, 350);

				currentColumn = 0;
				currentRow++;
			}
			// merge cells
			for (int i = 0; i < 3; i++) {
				sheet.mergeCells(currentColumn + i, currentRow - fp.faultModes.size(), currentColumn + i, currentRow - 1);
			}
		}

		// Σ
		currentColumn = 7;
		sheet.addCell(new Label(currentColumn, currentRow, "Σ " + String.format("%.3f", this.table.totalSPRF), this.bodyCF));
		sheet.mergeCells(currentColumn, currentRow, currentColumn + 1, currentRow);

		currentColumn += 4;
		sheet.addCell(new Label(currentColumn, currentRow, "Σ " + String.format("%.3f", this.table.totalMPFL), this.bodyCF));
		sheet.mergeCells(currentColumn, currentRow, currentColumn + 1, currentRow);

		// set row height
		sheet.setRowView(currentRow, 350);

		return ++currentRow;
	}

	/**
	 * Generate result
	 * @throws RowsExceededException, WriteException
	**/
	public int resultGenerate(WritableSheet sheet, int currentRow) throws RowsExceededException, WriteException {
		int currentColumn = 0;

		List<String> result = new ArrayList<String>();
		result.add("Total failure rate");
		result.add(this.table.totalFailureRate.toString() + " FIT");
		result.add("");
		result.add("SPFM");
		result.add(String.format("%.3f", this.table.SPFM * 100) + "%");
		result.add("Total Safety-Related");
		result.add(this.table.totalSafetyRelated.toString() + " FIT");
		result.add("");
		result.add("LFM");
		result.add(String.format("%.3f", this.table.LFM * 100) + "%");
		result.add("Total Non Safety-Related");
		result.add(this.table.totalNonSafetyRelated.toString() + " FIT");
		result.add("");
		result.add("PMHF");
		result.add(String.format("%.3f", this.table.PMHF) + " FIT");

		for (int i = 0; i < 3; i++) {
			currentColumn = 0;
			for (int j = 0; j < 5; j++) {
				sheet.addCell(new Label(currentColumn++, currentRow, result.get(i * 5 + j), this.resultCF));
			}
			for (int k = 0; k < 8; k++) {
				sheet.addCell(new Label(currentColumn + k, currentRow, "", this.resultCF));
			}
			// set row height
			sheet.setRowView(currentRow++, 350);
		}

		return currentRow;
	}

}
