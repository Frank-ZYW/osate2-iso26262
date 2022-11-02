package org.osate.iso26262.fmea;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.Property;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.StringLiteral;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.ui.dialogs.Dialog;
import org.osate.xtext.aadl2.properties.util.GetProperties;

public class FMEDAPI {

	FmeaBuilder fb;

	public static List<String> Get_Safety_Goal(ComponentInstance ci) {

		List<String> result = new ArrayList<String>();
		Property property;
		List<? extends PropertyExpression> propertyValues;
		BasicPropertyAssociation pa;
		property = GetProperties.lookupPropertyDefinition(ci, "FMEA_Prop", "Function");
		propertyValues = ci.getPropertyValueList(property);
		for (PropertyExpression pe : propertyValues) {
			RecordValue rv = (RecordValue) pe;
			EList<BasicPropertyAssociation> fields = rv.getOwnedFieldValues();

			String myfunc = null;
			pa = GetProperties.getRecordField(fields, "Func_Descrip");
			if (pa != null) {
				myfunc = ((StringLiteral) pa.getValue()).getValue();
				result.add(myfunc);
			}
		}
		return result;
	}

	public static List<ComponentInstance> Get_Calcul_Instance(ComponentInstance ci, String sg) {
		List<ComponentInstance> result = new ArrayList<ComponentInstance>();
		// 准备构建FMEA数据结构
		FmeaBuilder fb = new FmeaBuilder();
		// 构造结构树
		fb.Construct_structure_tree(ci);
		// 构造功能网
		fb.BuildFuncNet(fb.root_component);
		// 构造故障网
		fb.BuildFailureNet(fb.root_component);
		boolean findsg = false;

		for (Function fci : fb.root_component.functions.values()) {
			if (fci.myfunc.equals(sg)) {
				findsg = true;
				Set<ComponentInstance> result1 = new HashSet<ComponentInstance>();
				List<FailureMode> leaffmlist = new ArrayList<FailureMode>();
				for (FailureMode fmi : fci.ref_fail_modes) {
					fmi.getLeaf(leaffmlist);
				}
				for (FailureMode leaffmi : leaffmlist) {
					result1.add(leaffmi.ref_component.ci);
				}
				result.addAll(result1);
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
