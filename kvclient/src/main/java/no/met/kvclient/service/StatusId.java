package no.met.kvclient.service;

import java.util.function.IntPredicate;

/*-
 * useinfo(2) = 0 Originalverdi funnet i orden 
 * useinfo(2) = 1 Originalverdi noe mistenkelig (antagelig korrekt) 
 * useinfo(2) = 2 Originalverdi svært mistenkelig (antagelig feilaktig) 
 * useinfo(2) = 3 Originalverdi sikkert feilaktig 
 * useinfo(2) ∈ [4,8] < reservert > 
 * useinfo(2) = 9 Kvalitetsinformasjon ikke gitt
 */
public enum StatusId {
	All(StatusId::all), OnlyFailed(StatusId::onlyFailed), OnlyOk(StatusId::onlyOk);

	final private IntPredicate func;

	static boolean all(int useinfo2) {
		return true;
	}

	static boolean onlyFailed(int useinfo2) {
		return useinfo2 >= 2 ? true : false;
	}

	static boolean onlyOk(int useinfo2) {
		return useinfo2>=0 && useinfo2 < 2 ? true : false;
	}

	private StatusId(IntPredicate func) {
		this.func = func;
	}

	public boolean has(int useinfo2) {
		// useinfo2 < 0, means no value is given for the flag.
		//if (useinfo2!=9 && (useinfo2 < 0 || useinfo2 > 3))
		//	return false;

		return func.test(useinfo2);
	}
}
