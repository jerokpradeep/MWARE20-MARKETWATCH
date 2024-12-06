package in.codifi.mw.util;

public class ErrorMessageConstants {

	//Common Failed and Success Status
	public static final String SUCCESS_STATUS = "Success";
	public static final String FAILED_STATUS = "Failed";
	
	// Markert Watch Request Failed Message
	public static final String INVALID_REQUEST = "Request data cannot be invalid or empty";
	public static final String NEGATIVE_REQUEST = "Position must be a positive integer";
	public static final String INVALID_MARKETWATCH_REQUEST = "Invalid marketwatch";

	//Failed Message For Predefined Market Watch Table
	public static final String FAILED_CREATION ="Failed to create Market Watch";
	public static final String NOT_ABLE_TO_MW_UPDATE = "Not able to update marketwatch";
	public static final String NOT_ABLE_TO_MW_DELETED = "Not able to delete marketwatch";
	public static final String NULL_DATA_STATUS = null;
	public static final String NO_MW = "No Data";
	public static final String INVALID_ID = " MarketWatch Id or Position should not be more than 15 ";
	public static final String INVALID_ORDER = "SortedOrder should not be more than 60 ";
	public static final String INVALID_NAME="MarketWatch Name should not be more than 40 characters";
	public static final String INVALID_BINARY = "Should accept only 0 and 1";
	public static final String INVALID_TOKEN = "INVALID_TOKEN";
	public static final String INVALID_EXCHANGE = "INVALID_EXCHANGE";
	public static final String EXISTING_MWID = "MarketWatch with Same ID already exist";
	public static final String EXISTING_MWNAME = "MarketWatch with Same NAME already exist";

	
	//Failed Message For Predefined Market Watch Scrip Table
	public static final String FAILED_TO_ADD = "Failed to add Scrip";
	public static final String FAILED_DEL_SCRIP = "Failed to delete Scrip";
	public static final String SCRIP_NOT_SORTED = "Failed to sort Scrip";
	public static final String FAILED_INSERT_DATA = "Failed to insert data into scrip table";
	public static final String SCRIP_NOT_FOUND = "Scrip not found";
	public static final String INVALID_SCRIP_ID = "Scrip id is not available";
	public static final String NO_DATA_FOUND_STATUS = "Scrip not inserted";
	public static final String SCRIP_ISNOT_EDITABLE = "Not able to edit scrip";
		
	//Success Message For Predefined Market Watch Table
	public static final String MARKET_WATCH_CREATED = "Market Watch Created Successfully";
	public static final String SUCCESS_UPDATE ="Market Watch updated successfully";
	public static final String SUCCESS_DELETE ="Market Watch deleted successfully";
	
	//Success Message For Predefined Market Watch Scrip Table
	public static final String SUCCESS_TO_ADD = "Scrip added successfully";
	public static final String SCRIPS_SORTED = "Scrips sorted successfully";
	public static final String SUCCESS_DEL_SCRIP = "Scrip deleted Successfully";
	public static final String SUCESS_INSERT_SCRIP = "Successfully insert data into scrip table";

	public static final String LIMIT_REACHED_MW = "You have reached the maximum length of Match watch";
	public static final String INVALID_PARAMETER = "Invalid Parameter";


}
