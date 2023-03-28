package org.osate.iso26262.hazard;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.resources.OsateResourceUtil;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.ui.dialogs.Dialog;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class FileExport {

	EObject root;
	String fileExtension = "xls";


	File file;
	WritableWorkbook workbook;
	WritableSheet sheet;

	WritableCellFormat headcf;
	WritableCellFormat normalcf;
	WritableCellFormat redcf;

	String pathname;

	int curcolumn;
	int currow;

	FileExport(String reporttype, ComponentInstance root) {
		file = getReportPathName(reporttype, root);
	}

	public void setSuffix(String suffix) {
		pathname += suffix;
	}

	public void mergecell(int lc, int lr, int rc, int rr) {
		if (lr > rr || lc > rc) {
			return;
		}
		try {
			sheet.mergeCells(lc, lr, rc, rr);
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addcell(String content) {
		if (content == null || content.equals("\"\"")) {
			content="";
		}
		try {
			sheet.addCell(new Label(curcolumn, currow, content, normalcf));
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		curcolumn++;
	}

	public void addRedcell(String content) {
		if (content == null || content.equals("\"\"")) {
			content = "";
		}
		try {
			sheet.addCell(new Label(curcolumn, currow, content, redcf));

		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		curcolumn++;
	}

	public void addheadcell(String content) {
		if (content == null) {
			content = "";
		}
		try {
			sheet.addCell(new Label(curcolumn, currow, content, headcf));
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		curcolumn++;
	}

	public void nextline() {
		currow++;
		curcolumn = 0;
	}

	public void prepare() {
		try {
			curcolumn = 0;
			currow = 0;
//			file = new File(pathname);
//			file.createNewFile();
			// 创建工作簿
			workbook = Workbook.createWorkbook(file);
			// 创建sheet
			sheet = workbook.createSheet("SheetName", 0);
			// 设置单元格格式、构造汇报数据结构
			WritableFont wf = new WritableFont(WritableFont.createFont("宋体"), 11, WritableFont.NO_BOLD, false,
					UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK); // 定义格式 字体 下划线 斜体 粗体 颜色

			headcf = new WritableCellFormat(wf); // 单元格定义
			headcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);// 细边框
			headcf.setBackground(jxl.format.Colour.YELLOW); // 黄色底纹
			headcf.setAlignment(jxl.format.Alignment.CENTRE); // 水平居中
			headcf.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 垂直居中
			headcf.setWrap(true);// 自动换行

			normalcf = new WritableCellFormat(wf); // 单元格定义
			normalcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);// 细边框
			normalcf.setAlignment(jxl.format.Alignment.LEFT); // 水平靠左
			normalcf.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);// 垂直靠上
			normalcf.setWrap(true);// 自动换行

			redcf = new WritableCellFormat(wf); // 单元格定义
			redcf.setBackground(jxl.format.Colour.RED); // 红色底纹
			redcf.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);// 细边框
			redcf.setAlignment(jxl.format.Alignment.LEFT); // 水平靠左
			redcf.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);// 垂直靠上
			redcf.setWrap(true);// 自动换行
//			// 输出第一步（规划与准备）与2-6步的表头
//			ReportHead();
//			// 输出第二部结构分析
//			Report_Structure();
//			// 输出3-6步
//			Report_Remaining();
//
//			// 最后调整
//			Adj_Column_Width();
//			// 写文件
//			workbook.write();
//			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	public void saveToFile() {
		Adj_Column_Width();
		// 写文件

		try {
			workbook.write();
			workbook.close();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void Adj_Column_Width() {
		CellView cellView = new CellView();
		List<Integer> list = Stream.iterate(0, item -> item + 1).limit(19).collect(Collectors.toList());
		cellView.setAutosize(true); // 设置自动大小
		for (int i : list) {
			sheet.setColumnView(i, cellView);
		}
	}


	public File getReportPathName(String reporttype, ComponentInstance root) {

		String filename = null;
		reporttype = reporttype.replaceAll(" ", "");
		Resource res = root.eResource();
		URI uri = res.getURI();
		IPath path = OsateResourceUtil.toIFile(uri).getFullPath();
		path = path.removeFileExtension();

		filename = root instanceof SystemInstance
				? root.getComponentClassifier().getName().replaceAll("::", "-").replaceAll("\\.", "-")
				: root.getComponentInstancePath().replaceAll("::", "-").replaceAll("\\.", "-");
		filename += "_of_" + path.lastSegment();
		filename += "_" + reporttype;
		path = path.removeLastSegments(1).append("/reports/" + reporttype + "/" + filename);
		path = path.addFileExtension(fileExtension);
		AadlUtil.makeSureFoldersExist(path);

		// create report file
		IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		File file = ifile.getLocation().toFile();
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Dialog.showInfo("IOException e", e.toString());
			e.printStackTrace();
		}
		return file;
////		filename = path.lastSegment() + "__" + reporttype;
//		filename = root.getName() + "__" + reporttype;
//		path = path.removeLastSegments(1).append("/reports/" + reporttype + "/" + filename);
//		path = path.addFileExtension(fileExtension);
//		AadlUtil.makeSureFoldersExist(path);
//		String relative_pathname = path.toString();
//		String prj_abs_path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
//
//		return prj_abs_path + relative_pathname;
	}



}
