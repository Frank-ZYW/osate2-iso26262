package org.osate.iso26262.fmeda.report;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.osate.iso26262.fmeda.FmedaTable;

import jxl.write.WriteException;

public class FmedaReportGenerator {

	public ExcelReportGenerator excelReportGen; // Excel report generator
	public CsvReportGenerator csvReportGen; // CSV report generator

	public FmedaReportGenerator() throws WriteException {
		this.excelReportGen = new ExcelReportGenerator();
		this.csvReportGen = new CsvReportGenerator();
	}

	/**
	 * Set FmedaTable
	**/
	public void setFmedaTable(FmedaTable table) {
		this.excelReportGen.setFmedaTable(table);
		this.csvReportGen.setFmedaTable(table);
	}

	/**
	 * Write Excel report to hard disk
	 * @throws IOException, WriteException
	**/
	public void writeExcelReport(EObject target) throws WriteException, IOException {
		this.excelReportGen.writeReport(target);
	}

	/**
	 * Write CSV report to hard disk
	 * @throws IOException, WriteException, CoreException
	**/
	public void writeCsvReport(EObject target) throws WriteException, IOException, CoreException {
		this.csvReportGen.writeReport(target);
	}

}
