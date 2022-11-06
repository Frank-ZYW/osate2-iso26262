package org.osate.iso26262.fmeda.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.iso26262.fmeda.FmedaFaultMode;
import org.osate.iso26262.fmeda.FmedaProperty;
import org.osate.iso26262.fmeda.FmedaTable;
import org.osate.iso26262.fmeda.util.ReportUtil;

import jxl.write.WriteException;

public class CsvReportGenerator {

	public FmedaTable table;

	public CsvReportGenerator() throws WriteException {
		this.table = null;
	}

	/**
	 * Set FmedaTable
	**/
	public void setFmedaTable(FmedaTable table) {
		this.table = table;
	}

	/**
	 * Write report to hard disk
	 * @throws IOException, WriteException, CoreException
	**/
	public void writeReport(EObject target) throws IOException, WriteException, CoreException {
		// get report content
		String content = this.titleGenerate() + this.headGenerate() + this.bodyGenerate() + this.resultGenerate();

		// get report path
		IPath path = ReportUtil.getReportPath(target, "csv");

		// write report
		if (path != null) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			if (file != null) {
				final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
				if (file.exists()) {
					file.setContents(input, true, true, null);
				} else {
					AadlUtil.makeSureFoldersExist(path);
					file.create(input, true, null);
				}
			}
		}
	}

	/**
	 * Generate title
	**/
	public String titleGenerate() {
		StringBuffer title = new StringBuffer();

		title.append("Block Name,");
		title.append(this.table.blockName + ",");
		title.append("Safety Goal,");
		// use \" \" to avoid "," appears in safety goal
		title.append("\"" + this.table.safetyGoal + "\",");
		title.append("ASIL,");
		title.append(this.table.ASIL + "\n");

		return title.toString();
	}

	/**
	 * Generate head
	**/
	public String headGenerate() {
		StringBuffer head = new StringBuffer();

		head.append("Component Name,");
		head.append("Failure rate/FIT,");
		head.append("Safety-related component to be considered in the calculation?,");
		head.append("Failure Mode,");
		head.append("Failure mode distribution,");
		head.append("Failure mode that has the potential to violate the safety goal in absence of safety mechanisms?,");
		head.append("Safety mechanism(s) allowing to prevent the failure mode from violating the safety goal?,");
		head.append("Failure mode coverage wrt. violation of safety goal,");
		head.append("Residual or Single-Point Fault failure rate/FIT,");
		head.append("Failure mode that may lead to the violation of safety goal in combination with an independent failure of another component?,");
		head.append("Detection means? Safety mechanism(s) allowing to prevent the failure mode from being latent?,");
		head.append("Failure mode coverage wrt. Latent failures,");
		head.append("Latent Multiple-Point Fault failure rate/FIT\n");

		return head.toString();
	}

	/**
	 * Generate body
	**/
	public String bodyGenerate() {
		StringBuffer body = new StringBuffer();

		for (FmedaProperty fp : this.table.fpList) {
			for (FmedaFaultMode fm : fp.faultModes) {
				body.append(fp.componentName + ",");
				body.append(fp.failureRate.toString() + ",");
				body.append(fp.isSafetyRelated ? "YES," : "NO,");
				body.append(fm.modeName + ",");
				body.append(fm.distribution.toString() + "%,");
				body.append(fm.hasSPF ? "X," : ",");
				body.append(fm.hasSPF ? fm.spf_SM + "," : ",");
				body.append(fm.hasSPF ? fm.spf_DC.toString() + "%," : ",");
				body.append(String.format("%.3f", fm.sprf) + ",");
				body.append(fm.hasMPF ? "X," : ",");
				body.append(fm.hasMPF ? fm.mpf_SM + "," : ",");
				body.append(fm.hasMPF ? fm.mpf_DC.toString() + "%," : ",");
				body.append(String.format("%.3f", fm.mpfl) + "\n");
			}
		}

		return body.toString();
	}

	/**
	 * Generate result
	**/
	public String resultGenerate() {
		StringBuffer result = new StringBuffer();

		result.append("Total failure rate," + this.table.totalFailureRate + " FIT,");
		result.append("Total Safety-Related," + this.table.totalSafetyRelated + " FIT,");
		result.append("Total Non Safety-Related," + this.table.totalNonSafetyRelated + " FIT,");
		result.append("SPFM," + String.format("%.3f", this.table.SPFM * 100) + "%,");
		result.append("LFM," + String.format("%.3f", this.table.LFM * 100) + "%,");
		result.append("PMHF," + String.format("%.3f", this.table.PMHF) + " FIT,");
		result.append(this.table.reachASILLevel ? "Accept" : "Reject" + "\n");

		return result.toString();
	}

}
