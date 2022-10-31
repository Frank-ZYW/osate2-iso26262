package org.osate.iso26262.fmeda.util;

import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.osate.aadl2.modelsupport.resources.OsateResourceUtil;

import jxl.format.UnderlineStyle;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WriteException;

public class ReportUtil {

	/**
	 * Get the save path of report file
	**/
	public static IPath getReportPath(EObject root) {
		String filename = null;

		Resource res = root.eResource();
		URI uri = res.getURI();
		IPath path = OsateResourceUtil.toIFile(uri).getFullPath();

		path = path.removeFileExtension();
		filename = path.lastSegment() + "_fmeda";
		path = path.removeLastSegments(1).append("/reports/fmeda/" + filename);
		path = path.addFileExtension("xls");
		return path;
	}

	/**
	 * Get cell format of title
	**/
	public static WritableCellFormat getTitleCellFormat() throws WriteException {
		// define font & font size & bold style & underline style & color
		WritableFont wf = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);

		WritableCellFormat titleCF = new WritableCellFormat(wf);
		titleCF.setBackground(jxl.format.Colour.YELLOW); // yellow background
		titleCF.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.NONE); // no border
		titleCF.setAlignment(jxl.format.Alignment.LEFT); // alignment: left
		titleCF.setVerticalAlignment(jxl.format.VerticalAlignment.TOP); // vertical alignment: top
		titleCF.setWrap(true); // auto wrap

		return titleCF;
	}

	/**
	 * Get cell format of head
	**/
	public static WritableCellFormat getHeadCellFormat() throws WriteException {
		WritableFont wf = new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);

		WritableCellFormat headCF = new WritableCellFormat(wf);
		headCF.setBackground(jxl.format.Colour.GRAY_25); // gray background
		headCF.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.NONE); // no border
		headCF.setAlignment(jxl.format.Alignment.CENTRE); // alignment: center
		headCF.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE); // vertical alignment: center
		headCF.setWrap(true); // auto wrap

		return headCF;
	}

	/**
	 * Get cell format of body
	**/
	public static WritableCellFormat getBodyCellFormat() throws WriteException {
		WritableFont wf = new WritableFont(WritableFont.ARIAL, 11, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);

		WritableCellFormat bodyCF = new WritableCellFormat(wf);
		bodyCF.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.NONE); // no border
		bodyCF.setAlignment(jxl.format.Alignment.CENTRE); // alignment: center
		bodyCF.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE); // vertical alignment: center
		bodyCF.setWrap(true); // auto wrap

		return bodyCF;
	}

	/**
	 * Get cell format of result
	**/
	public static WritableCellFormat getResultCellFormat() throws WriteException {
		WritableFont wf = new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);

		WritableCellFormat resultCF = new WritableCellFormat(wf);
		resultCF.setBackground(jxl.format.Colour.TAN); // gray background
		resultCF.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.NONE); // no border
		resultCF.setAlignment(jxl.format.Alignment.CENTRE); // alignment: center
		resultCF.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE); // vertical alignment: center
		resultCF.setWrap(true); // auto wrap

		return resultCF;
	}

}
