package kvalobs.service;

import java.util.function.IntPredicate;

/*-
 * useinfo(0) = 0	< reservert >
 * useinfo(0) = 1	QC1, QC2 og HQC er gjennomført
 * useinfo(0) = 2	QC2 og HQC er gjennomført (ikke QC1)
 * useinfo(0) = 3	QC1 og HQC er gjennomført (ikke hele QC2)
 * useinfo(0) = 4	HQC er gjennomført (ikke QC1, ikke hele QC2)
 * useinfo(0) = 5	QC1 og QC2 er gjennomført (ikke HQC)
 * useinfo(0) = 6	QC2 er gjennomført (ikke QC1, ikke HQC)
 * useinfo(0) = 7	QC1 er gjennomført (ikke hele QC2, ikke HQC)
 * useinfo(0) = 8	Aggregert verdi, grunnlagsdata er kontrollert
 * useinfo(0) = 9	Informasjon om kontrollnivå ikke gitt
 */

public enum QcId {
	QC1(QcId::qc1Func), QC2(QcId::qc2Func), HQC(QcId::hqcFunc);

	final private IntPredicate func;

	static boolean qc1Func(int useinfo0) {
		if (useinfo0 == 2 || useinfo0 == 4 || useinfo0 == 6)
			return false;
		else
			return true;
	}

	static boolean qc2Func(int useinfo0) {
		if (useinfo0 == 2 || useinfo0 == 5 || useinfo0 == 6)
			return true;
		else
			return false;
	}

	static boolean hqcFunc(int useinfo0) {
		if (useinfo0 == 2 || useinfo0 == 3 || useinfo0 == 4)
			return true;
		else
			return false;
	}

	private QcId(IntPredicate func) {
		this.func = func;
	}

	public boolean has(int useinfo0) {
		// useinfo0 < 0, means no value is given for the flag.
		if (useinfo0 == 1 || useinfo0 == 8)
			return true;
		else if (useinfo0 <= 0 || useinfo0 >= 9)
			return false;
		return func.test(useinfo0);
	}
}
