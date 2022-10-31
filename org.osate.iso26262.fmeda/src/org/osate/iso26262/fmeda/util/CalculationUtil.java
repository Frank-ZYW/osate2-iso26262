package org.osate.iso26262.fmeda.util;

import org.osate.iso26262.fmeda.FmedaFaultMode;
import org.osate.iso26262.fmeda.FmedaProperty;
import org.osate.iso26262.fmeda.FmedaTable;

public class CalculationUtil {

	/**
	 * Calculate SPF/RF & MPF,L for each component each line
	**/
	public static void lineCalc(FmedaProperty fp) {
		for (FmedaFaultMode fm : fp.faultModes) {
			fm.CalcInit();
			// calculate SPF/RF
			if (fm.hasSPF) {
				fm.sprf = fp.failureRate * fm.distribution / 100 * (1 - fm.spf_DC / 100);
			}
			// calculate MPF,L
			if (fm.hasMPF) {
				fm.mpfl = (fp.failureRate * fm.distribution / 100 - fm.sprf) * (1 - fm.mpf_DC / 100);
			}
		}
	}

	/**
	 * Calculate FMEDA index, includes
	 * total Failure Rate & Safety Related Rate & NonSafety Related Rate & SPFM & LFM & PMHF
	**/
	public static void fmedaCalc(FmedaTable fmedaTb) {
		fmedaTb.CalcInit();
		for (FmedaProperty fp : fmedaTb.fpList) {
			CalculationUtil.lineCalc(fp);

			// total SPF/RF & MPF,L of each component each line
			for (FmedaFaultMode fm : fp.faultModes) {
				fmedaTb.totalSPRF += fm.sprf;
				fmedaTb.totalMPFL += fm.mpfl;
			}

			// total FailureRate & SafetyRelatedRate
			fmedaTb.totalFailureRate += fp.failureRate;
			if (fp.isSafetyRelated) {
				fmedaTb.totalSafetyRelated += fp.failureRate;
			}
		}
		// total NonSafetyRelatedRate & SPFM & LFM & PMHF
		fmedaTb.totalNonSafetyRelated = fmedaTb.totalFailureRate - fmedaTb.totalSafetyRelated;
		fmedaTb.SPFM = 1 - (fmedaTb.totalSPRF / fmedaTb.totalSafetyRelated);
		fmedaTb.LFM = 1 - (fmedaTb.totalMPFL / (fmedaTb.totalSafetyRelated - fmedaTb.SPFM));
		fmedaTb.PMHF = fmedaTb.totalSPRF + fmedaTb.totalMPFL;
	}

}
