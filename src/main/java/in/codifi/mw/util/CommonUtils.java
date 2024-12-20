/**
 * 
 */
package in.codifi.mw.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import in.codifi.mw.config.ApplicationProperties;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class CommonUtils {

	@Inject
	ApplicationProperties properties;

	private static final String POSITIVE_WHOLENUMBER_REGEX = "^[1-9]\\d*(\\.0+)?$";

	public boolean isValidExch(String input) {
		// Use a regex to check if the input matches any of the valid values
		if (properties.isExchfull()) {
			return input.toUpperCase().matches("BSECURR|BSEFO|BSEEQ|NSECURR|MCXCOMM|NSEFO|NSEEQ|NSECOMM");
		} else {
			return input.toUpperCase().matches("NFO|NCO|NSE|CDS|NCDEX|MCX|BFO|BCD|BCO|BSE");

		}

	}

	public boolean isValidMWRename(String input) {
		// Use a regex to check if the input matches any of the valid values
		return input.toUpperCase().matches("NIFTY 50|NIFTY50|RECENTLYVIEWED|RECENTLY VIEWED|MY STOCK|MYSTOCK");
	}

	public boolean isValidExchSegment(String input) {
		// Use a regex to check if the input matches any of the valid values
		return input.toUpperCase().matches("BCD|BFO|BSE|CDS|MCX|NFO|NSE|NCO");
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

	public boolean validateToken(String token) {
		int TOKEN_MAX_LENGTH = 6;
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
			} else if (exchSeg.equalsIgnoreCase("nse_idx")) {
				exch = "NSE";
			} else if (exchSeg.equalsIgnoreCase("bse_idx")) {
				exch = "BSE";
			} else if (exchSeg.equalsIgnoreCase("mcx_idx")) {
				exch = "MCX";
			}
		} catch (Exception e) {
			return exch;
		}
		return exch;
	}

	/**
	 * Method to find the exchange segment for the given exchange
	 * 
	 * @author Gowrisankar
	 * @param exch
	 * @return
	 */
	public String getExchangeSegmentNameIIFL(String exch) {
		String exchSegment = "";
		try {
			exch = exch.trim();
			if (exch.equalsIgnoreCase("NSEFO")) {
				exchSegment = "nse_fo";
			} else if (exch.equalsIgnoreCase("NSECOMM")) {
				exchSegment = "nse_com";
			} else if (exch.equalsIgnoreCase("NSEEQ")) {
				exchSegment = "nse_cm";
			} else if (exch.equalsIgnoreCase("NSECURR")) {
				exchSegment = "cde_fo";
			} else if (exch.equalsIgnoreCase("NCDEXCOMM")) {
				exchSegment = "ncx_fo";
			} else if (exch.equalsIgnoreCase("MCXCOMM")) {
				exchSegment = "mcx_fo";
			} else if (exch.equalsIgnoreCase("BSEFO")) {
				exchSegment = "bse_fo";
			} else if (exch.equalsIgnoreCase("BSECURR")) {
				exchSegment = "bcs_fo";
			} else if (exch.equalsIgnoreCase("BSECOMM")) {
				exchSegment = "bse_com";
			} else if (exch.equalsIgnoreCase("BSEEQ")) {
				exchSegment = "bse_cm";
			}
		} catch (Exception e) {
			return exchSegment;
		}
		return exchSegment;
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

	public String getExchangeNameContract(String exchSeg) {
		String exch = "";
		try {
			if (exchSeg.equalsIgnoreCase("NSEFO")) {
				exch = "NFO";
			} else if (exchSeg.equalsIgnoreCase("NSECOMM")) {
				exch = "NCO";
			} else if (exchSeg.equalsIgnoreCase("NSEEQ")) {
				exch = "NSE";
			} else if (exchSeg.equalsIgnoreCase("NSECUR")) {
				exch = "CDS";
			} else if (exchSeg.equalsIgnoreCase("NCDEXCOMM")) {
				exch = "NCDEX";
			} else if (exchSeg.equalsIgnoreCase("MCXCOMM")) {
				exch = "MCX";
			} else if (exchSeg.equalsIgnoreCase("BSEFO")) {
				exch = "BFO";
			} else if (exchSeg.equalsIgnoreCase("BSECURR")) {
				exch = "BCD";
			} else if (exchSeg.equalsIgnoreCase("BSECOMM")) {
				exch = "BCO";
			} else if (exchSeg.equalsIgnoreCase("BSEEQ")) {
				exch = "BSE";
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

	public boolean isBetweenOneAndhundred(int mwId) {
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

	public boolean checkExchangeIsValid(String[] reqModel) {
		// Hardcoded exchange values as List<String>
		List<String> hardcodedExchanges = new ArrayList<>();
		hardcodedExchanges.add("NSEEQ");
		hardcodedExchanges.add("NSEFO");
		hardcodedExchanges.add("BSEEQ");
		hardcodedExchanges.add("BSEFO");
		hardcodedExchanges.add("NSECURR");
		hardcodedExchanges.add("BSECURR");
		hardcodedExchanges.add("MCXCOMM");
		hardcodedExchanges.add("NSECOMM");
		hardcodedExchanges.add("ALL");

		// Check if any of the reqModel values are in the hardcodedExchanges list
		for (String exchange : reqModel) {
			if (!hardcodedExchanges.contains(exchange.trim().toUpperCase())) {
				return false; // If any value is not found in the list, return false
			}
		}
		return true; // If all values are found in the list, return true
	}

}
