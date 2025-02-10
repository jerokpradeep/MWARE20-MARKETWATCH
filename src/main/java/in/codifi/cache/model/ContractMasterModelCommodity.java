/**
 * 
 */
package in.codifi.cache.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
public class ContractMasterModelCommodity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String exch;
	private String segment;
	private String token;
	private String alterToken;
	private String symbol;
	private String tradingSymbol;
	private String formattedInsName;
	private String instrumentName;
	private String isin;
	private String groupName;
	private String insType;
	private String optionType;
	private String strikePrice;
	private Date expiry;
	private String lotSize;
	private String tickSize;
	private String freezQty;
	private String pdc;
	private String weekTag;
	private String companyName;
	private String optionFlag;
	private String minPrice;
	private String maxPrice;
	private String priceUnit;
	private String qtyunit;
	private String deliveryUnit;
	private String contractStartDate;
	private String tenderStartDate;
	private String tenderEndDate;
	private String deliveryStartDate;
	private String deliveryEndDate;
	private String lastTradingDate;

}
