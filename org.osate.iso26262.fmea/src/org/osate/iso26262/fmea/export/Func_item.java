package org.osate.iso26262.fmea.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.osate.iso26262.fmea.FailureMode;
import org.osate.iso26262.fmea.Function;

public class Func_item {
	Integer suplevel;
	Integer sublevel;
	Function myfunc;
	List<Function> supfunc = new ArrayList<Function>();
	List<Function> subfunc = new ArrayList<Function>();
	public HashMap<Function, Integer> levelmap = new HashMap<Function, Integer>();
	List<Error_item> erroritems = new ArrayList<Error_item>();
	Integer maxrows;

	Func_item(Function fi) {
		myfunc = fi;
		levelmap.put(myfunc, 0);

		for (Function fii : myfunc.func_effect) {
			Build_supfunc(fii, 1);
		}
		for (Function fii : myfunc.func_cause) {
			Build_subfunc(fii, 1);
		}
		Build_Erroritem();
		Calculate_maxrows();
	}

	void Build_supfunc(Function fi, int level) {
		if(not_Layer_overflow(suplevel,level))
		{
			supfunc.add(fi);
			levelmap.put(fi, level);
			for(Function fii:fi.func_effect)
			{
				Build_supfunc(fii, level + 1);

			}
		}
	}

	void Build_subfunc(Function fi, int level) {
		if (not_Layer_overflow(sublevel, level)) {
			subfunc.add(fi);
			levelmap.put(fi, level);
			for (Function fii : fi.func_cause) {
				Build_subfunc(fii, level + 1);
			}
		}
	}

	void Build_Erroritem() {
		for (FailureMode fmi : myfunc.ref_fail_modes) {
			erroritems.add(new Error_item(fmi));
		}
	}

	boolean not_Layer_overflow(Integer maxlevel, Integer mylevel) {
		boolean result = true;
		if (maxlevel != null && mylevel > maxlevel) {
			result=false;
		}
		return result;
	}

	void Calculate_maxrows() {
		Integer errorrows = 0;
		for (Error_item ei : erroritems) {
			errorrows = errorrows + ei.maxrows;
		}
		errorrows = Math.max(errorrows, 1);
		maxrows = 0;
		maxrows = Math.max(errorrows, supfunc.size());
		maxrows = Math.max(maxrows, subfunc.size());

		if (maxrows > errorrows && erroritems.size() > 0) {
			Error_item last = erroritems.get(erroritems.size() - 1);
			last.maxrows = maxrows - (errorrows - last.maxrows);
			last.Updatemaxrows();
		}

	}

	void Updatemaxrows() {
		if (erroritems.size() > 0) {
			Integer errorrows = 0;
			for (Error_item ei : erroritems) {
				errorrows = errorrows + ei.maxrows;
			}
			if (maxrows > errorrows) {
				Error_item last = erroritems.get(erroritems.size() - 1);
				System.out.println("Func maxrows::" + maxrows + "     last.maxrows::" + last.maxrows + "  Updatelast::"
						+ (maxrows - (errorrows - last.maxrows)));
				last.maxrows = maxrows - (errorrows - last.maxrows);
				last.Updatemaxrows();
			}
		}
	}


}
