package org.osate.iso26262.fmea.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.osate.iso26262.fmea.FailureMode;

import javafx.util.Pair;

public class Error_item {
	Integer suplevel;
	Integer sublevel;
	FailureMode myerror;
	List<FailureMode> superror = new ArrayList<FailureMode>();
	List<FailureMode> suberror = new ArrayList<FailureMode>();
	HashMap<FailureMode, Integer> levelmap = new HashMap<FailureMode, Integer>();
	List<Pair<Integer, Integer>> rowandrownums = new ArrayList<Pair<Integer, Integer>>();
	Integer subrownums;
	Integer maxrows;

	Error_item(FailureMode fmi) {
		myerror = fmi;
		levelmap.put(myerror, 0);

		for (FailureMode fii : myerror.failure_effect) {
			Build_superror(fii, 1);
		}
		subrownums = 0;
		rowandrownums.add(new Pair<Integer, Integer>(0, 1));
		for (FailureMode fii : myerror.failure_cause) {
			Build_suberror(fii, 1);
		}
		rowandrownums.remove(0);
		subrownums = Math.max(subrownums, 1);
		maxrows = Math.max(subrownums, superror.size());
	}

	void Build_superror(FailureMode fi, int level) {
		if (not_Layer_overflow(suplevel, level)) {
			superror.add(fi);
			levelmap.put(fi, level);
			for (FailureMode fii : fi.failure_effect) {
				Build_superror(fii, level + 1);

			}
		}
	}

	void Build_suberror(FailureMode fi, int level) {
		if (not_Layer_overflow(sublevel, level)) {
			suberror.add(fi);
			Pair<Integer, Integer> lastPair = getLastPair();
			int rows = Math.max(fi.optimizations.size(), 1);
			rowandrownums.add(new Pair<Integer, Integer>(lastPair.getKey() + lastPair.getValue(), rows));
			subrownums = subrownums + rows;
			levelmap.put(fi, level);
			for (FailureMode fii : fi.failure_cause) {
				Build_suberror(fii, level + 1);
			}
		}
	}

	boolean not_Layer_overflow(Integer maxlevel, Integer mylevel) {
		boolean result = true;
		if (maxlevel != null && mylevel > maxlevel) {
			result = false;
		}
		return result;
	}

	public Pair<Integer, Integer> getLastPair() {
		return rowandrownums.get(rowandrownums.size() - 1);
	}

	void Updatemaxrows() {
		if (rowandrownums.size() > 0) {
			Pair<Integer, Integer> last = getLastPair();
			Integer lastrow = last.getKey();
			rowandrownums.remove(rowandrownums.size() - 1);
			rowandrownums.add(new Pair<Integer, Integer>(lastrow, maxrows - (lastrow - 1)));

		}

	}

}
