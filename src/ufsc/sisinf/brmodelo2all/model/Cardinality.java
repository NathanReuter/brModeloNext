package ufsc.sisinf.brmodelo2all.model;

public enum Cardinality {

	ONE_ONE, ZERO_ONE, ONE_N, ZERO_N;

	public static String getText(Cardinality cardinality) {
		switch (cardinality) {
		case ZERO_ONE:
			return "(0,1)";
		case ZERO_N:
			return "(0,n)";
		case ONE_ONE:
			return "(1,1)";
		case ONE_N:
			return "(1,n)";
		default:
			return "";
		}
	}

	public static Cardinality getValue(String text) {
		Cardinality cardinality = null;
		if (text == "(0,1)") {
			cardinality = ZERO_ONE;
		} else if (text == "(0,n)") {
			cardinality = ZERO_N;
		} else if (text == "(1,1)") {
			cardinality = ONE_ONE;
		} else if (text == "(1,n)") {
			cardinality = ONE_N;
		}

		return cardinality;
	}

	public static boolean minor(Cardinality first, Cardinality second) {
		boolean result = false;

		switch (first) {
		case ONE_ONE:
			result = second != ONE_ONE;
			break;

		case ZERO_ONE:
			result = second != ONE_ONE && second != ZERO_ONE;
			break;

		case ONE_N:
			result = second == ZERO_N;

		}

		return result;
	}

	public static boolean major(Cardinality first, Cardinality second) {
		boolean result = false;

		switch (first) {
		case ZERO_N:
			result = second != ZERO_N;
			break;

		case ONE_N:
			result = second != ZERO_N && second != ONE_N;
			break;

		case ZERO_ONE:
			result = second == ONE_ONE;

		}

		return result;
	}
}
