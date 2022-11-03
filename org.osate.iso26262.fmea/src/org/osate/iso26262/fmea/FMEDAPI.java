package org.osate.iso26262.fmea;

import java.util.ArrayList;
import java.util.List;

import org.osate.aadl2.instance.ComponentInstance;
import org.osate.ui.dialogs.Dialog;

public class FMEDAPI {

	FmeaBuilder fb;
	ComponentInstance ci;

	FMEDAPI(ComponentInstance ci) {
		this.ci = ci;
		// 准备构建FMEA数据结构
		FmeaBuilder fb = new FmeaBuilder();
		// 构造结构树
		fb.Construct_structure_tree(ci);
		// 同时构造故障网与功能网
		fb.BuildFailureAndFuncNet(fb.root_component);
	}

	public List<String> Get_Safety_Goal() {
		List<String> result = new ArrayList<String>();
		for (Function fi : fb.root_component.functions.values()) {
			result.add(fi.funcname);
		}
		return result;
	}

	public List<ComponentInstance> Get_Calcul_Instance(ComponentInstance ci, String sg) {
		List<ComponentInstance> result = new ArrayList<ComponentInstance>();

		boolean findsg = false;

		for (Function fci : fb.root_component.functions.values()) {
			if (fci.funcname.equals(sg)) {
				findsg = true;

				List<FailureMode> leaffmlist = new ArrayList<FailureMode>();
				for (FailureMode fmi : fci.ref_fail_modes) {
					fmi.getLeaf(leaffmlist);
				}
				for (FailureMode leaffmi : leaffmlist) {
					result.add(leaffmi.ref_component.ci);
				}
			}
		}
		if (findsg == false) {
			Dialog.showInfo("Get_Calcul_Instance",
					"Can't find Safety Goal \"" + sg + "\" in Component \"" + ci.getName() + "\"");
			return null;
		}
		if (result.size() == 0) {
			Dialog.showInfo("Get_Calcul_Instance", "Can't find related sub component in Safety Goal \"" + sg + "\"");
		}
		return result;
	}

}
