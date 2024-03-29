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
	 * Determine if the target reaches the ASIL level
	**/
	public static Boolean levelReachable(FmedaTable fmedaTb) {
		switch (fmedaTb.ASIL) {
		case "D":
			// SPFM ≥ 99%, LFM ≥ 90%, PMHF < 10^−8/h = 10 Fit
			if (fmedaTb.SPFM >= 0.99 && fmedaTb.LFM >= 0.9 && fmedaTb.PMHF < 10) {
				return true;
			}
			return false;
		case "C":
			// SPFM ≥ 97%, LFM ≥ 80%, PMHF < 10^−7/h = 100 Fit
			if (fmedaTb.SPFM >= 0.97 && fmedaTb.LFM >= 0.8 && fmedaTb.PMHF < 100) {
				return true;
			}
			return false;
		case "B":
			// SPFM ≥ 90%, LFM ≥ 60%, PMHF < 10^−7/h = 100 Fit
			if (fmedaTb.SPFM >= 0.9 && fmedaTb.LFM >= 0.6 && fmedaTb.PMHF < 100) {
				return true;
			}
			return false;
		default:
			return true;
		}
	}

	/**
	 * Calculate FMEDA index, includes
	 * total Failure Rate & Safety Related Rate & NonSafety Related Rate & SPFM & LFM & PMHF
	 * & determine if the target reaches the ASIL level
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

		// level reachable check
		fmedaTb.reachASILLevel = CalculationUtil.levelReachable(fmedaTb);
	}

}
