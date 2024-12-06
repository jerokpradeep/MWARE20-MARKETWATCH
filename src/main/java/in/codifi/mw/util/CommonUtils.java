/**
 * 
 */
package in.codifi.mw.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class CommonUtils {
	private static final String POSITIVE_WHOLENUMBER_REGEX = "^[1-9]\\d*(\\.0+)?$";

	
	public static boolean isValidExch(String input) {
		// Use a regex to check if the input matches any of the valid values
		return input.matches("BCD|BFO|BSE|CDS|MCX|NFO|NSE|NCO");
	}

	public boolean checkThisIsTheNumber(String token) {
		int TOKEN_MAX_LENGTH = 8;
		String TOKEN_ALLOWED_CHARACTERS_REGEX = "^\\d+$";
		if (StringUtil.isNullOrEmpty(token)) {
			return false;
		}
		if (token.length() > TOKEN_MAX_LENGTH) {
			return false;
		}
		return Pattern.matches(TOKEN_ALLOWED_CHARACTERS_REGEX, token);
	}

	/**
	 * @param mwId
	 * @return
	 */
	public boolean isBetweenOneAndFive(int mwId) {
		return mwId >= 1 && mwId <= 5;
	}

	public String getExchangeName(String exchSeg) {
		String exch = "";
		try {
			if (exchSeg.equalsIgnoreCase("nse_fo")) {
				exch = "NFO";
			} else if (exchSeg.equalsIgnoreCase("nse_com")) {
				exch = "NCO";
			} else if (exchSeg.equalsIgnoreCase("nse_cm")) {
				exch = "NSE";
			} else if (exchSeg.equalsIgnoreCase("cde_fo")) {
				exch = "CDS";
			} else if (exchSeg.equalsIgnoreCase("ncx_fo")) {
				exch = "NCDEX";
			} else if (exchSeg.equalsIgnoreCase("mcx_fo")) {
				exch = "MCX";
			} else if (exchSeg.equalsIgnoreCase("bse_fo")) {
				exch = "BFO";
			} else if (exchSeg.equalsIgnoreCase("bcs_fo")) {
				exch = "BCD";
			} else if (exchSeg.equalsIgnoreCase("bse_com")) {
				exch = "BCO";
			} else if (exchSeg.equalsIgnoreCase("bse_cm")) {
				exch = "BSE";
			}
		} catch (Exception e) {
			return exch;
		}
		return exch;
	}

	public String getExchangeNameIIFL(String exchSeg) {
		String exch = "";
		try {
			if (exchSeg.equalsIgnoreCase("NFO")) {
				exch = "NSEFO";
			} else if (exchSeg.equalsIgnoreCase("NCO")) {
				exch = "NSECOMM";
			} else if (exchSeg.equalsIgnoreCase("NSE")) {
				exch = "NSEEQ";
			} else if (exchSeg.equalsIgnoreCase("CDS")) {
				exch = "NSECURR";
			} else if (exchSeg.equalsIgnoreCase("ncx_fo")) {
				exch = "NCDEXCOMM";
			} else if (exchSeg.equalsIgnoreCase("MCX")) {
				exch = "MCXCOMM";
			} else if (exchSeg.equalsIgnoreCase("BFO")) {
				exch = "BSEFO";
			} else if (exchSeg.equalsIgnoreCase("BCD")) {
				exch = "BSECURR";
			} else if (exchSeg.equalsIgnoreCase("bse_com")) {
				exch = "BSECOMM";
			} else if (exchSeg.equalsIgnoreCase("BSE")) {
				exch = "BSEEQ";
			}
		} catch (Exception e) {
			return exch;
		}
		return exch;
	}

	public static boolean isAlphanumeric(String input) {
		if (input == null || input.isEmpty()) {
			return false; // Consider throwing an IllegalArgumentException if needed
		}

		// Pattern to match only alphanumeric characters (letters and digits)
		Pattern pattern = Pattern.compile("^[a-zA-Z0-9 ]+$");
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * @param mwId
	 * @return
	 */
	public boolean isBetweenOneAndFifty(int mwId) {
		return mwId >= 1 && mwId <= 50;
	}

	/**
	 * @param mwName
	 * @return
	 */
	public boolean isEmptyOrWhitespace(String mwName) {
		return mwName == null || mwName.chars().allMatch(Character::isWhitespace);
	}

	public static boolean isOnlyInteger(String input) {
		return input != null && input.matches("\\d+");
	}
	
	public boolean isBetweenOneAndhundred (int mwId) {
		return mwId >= 1 && mwId <= 100;
	}

	public boolean isBetweenOneAndfifty(int mwId) {
		return mwId >= 1 && mwId <= 50;
	}
	
	public boolean isPositiveWholeNumber(String input) {
		// Remove leading zeros before the decimal point or before the entire number
		String trimmed = input.trim().replaceFirst("^0+(?!$)", "");

		// If the string is empty after removing leading zeros, it was just "0"
		if (trimmed.isEmpty() || trimmed.equals("0")) {
			return false;
		}

		return trimmed.matches(POSITIVE_WHOLENUMBER_REGEX);
	}
}
