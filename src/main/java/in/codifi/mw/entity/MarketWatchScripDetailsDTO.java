/**
 * 
 */
package in.codifi.mw.entity;

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
public class MarketWatchScripDetailsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private String userId;

	private int mwId;

	private String formattedName;

	private String ex;

	private String exSeg;

	private String token;

	private String symbol;

	private String tradingSymbol;

	private String groupName;

	private String instrumentType;

	private String optionType;

	private String strikePrice;

	private String pdc;

	private Date expDt;

	private String lotSize;

	private String tickSize;

	private int sortingOrder;

	private String alterToken;

	private String weekTag;

	private String strike_price;
}
