package ch.wildwatcher.control;

import javax.ejb.Stateless;

@Stateless
public class StringConverter {
	public static final String FALSE = "false";
	public static final String TRUE = "true";

	public String removeQuotes(String resultString) {
		return resultString.substring(1, resultString.length() - 1);
	}

	public Boolean getBoolean(String resultString) {
		Boolean booleanResult = null;
		if (resultString.trim().equalsIgnoreCase(TRUE) || resultString.trim().equalsIgnoreCase(FALSE)) {
			try {
				booleanResult = Boolean.parseBoolean(resultString);
			} catch (Exception e) {
			}
		}
		return booleanResult;
	}

	public Double getDouble(String resultString) {
		Double doubleResult = null;
		try {
			doubleResult = Double.parseDouble(resultString);
		} catch (Exception e) {
		}
		return doubleResult;
	}

	public Integer getInt(String resultString) {
		Integer integerResult = null;
		try {
			integerResult = Integer.parseInt(resultString);
		} catch (Exception e) {
		}
		return integerResult;
	}

}
