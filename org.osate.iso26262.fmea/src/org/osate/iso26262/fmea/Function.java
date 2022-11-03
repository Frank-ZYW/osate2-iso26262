package org.osate.iso26262.fmea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osate.ui.dialogs.Dialog;

public class Function {
	public String id;
	public String funcname;

	public Structure ref_component;
//	public List<String> func_require;
	public List<Function> func_effect = new ArrayList<Function>();
	public List<Function> func_cause = new ArrayList<Function>();
	public List<FailureMode> ref_fail_modes = new ArrayList<FailureMode>();

	public Function(String id, String myfunc, Structure ref_component, List<String> func_require) {
		System.out.print("	new Functions::::  id-" + id + "  describe-" + myfunc);
		this.id = id;
		this.funcname = myfunc;
		this.ref_component = ref_component;
//		this.func_require = func_require;
//		for (String si : func_require) {
//			System.out.print(si + ",");
//		}
		System.out.println("");
	}


	public void Print(String indent) {
		System.out.print(indent + "  |<-Structure:: " + ref_component.ci.getName());
		System.out.print("  |id:: " + id);
		System.out.print("  |descri:: " + funcname);
//		if(func_require.size()>0) {
//			System.out.print("  |func_req:: ");
//		for (String si : func_require) {
//			System.out.print(si + ",");
//		}}
	if (ref_fail_modes.size() > 0) {
		System.out.print("  |ref_fail_modes:: ");
		for (FailureMode fi : ref_fail_modes) {
			System.out.print(fi.id + ",");
		}
	}
	if (func_effect.size() > 0) {
		System.out.print("  | (");
		for (Function fi : func_effect) {
			System.out.print(fi.ref_component.ci.getName() + "." + fi.id + " , ");
		}
		System.out.print(")<<---");
		}
	if (func_cause.size() > 0) {
		System.out.print("  |--->>(");
		for (Function fi : func_cause) {
			System.out.print(fi.ref_component.ci.getName() + "." + fi.id + " , ");
		}
		System.out.print(")");
	}
	System.out.println("");
	}



	public List<String> SplitFuncRequire(String func_require) {

		if (func_require == null) {
			return null;
		}

		List<String> requirelist = Arrays.asList(func_require.split("\\.")); // 分割字符串.
		if (requirelist.size() > 1) {
			return requirelist;
		} else {
			Dialog.showInfo("SplitFuncRequire", "No ‘.’ character to split");
			return null;

		}
	}

}

