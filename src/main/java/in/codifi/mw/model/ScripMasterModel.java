/**
 * 
 */
package in.codifi.mw.model;

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
public class ScripMasterModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String exchange_type; 
	private String exch_symbol; 
	private String trading_symbol; 
	private String series; 
	private String isin; 
	private String instrument_type; 
	private String strike_price; 
	private String option_type; 
	private String expiry; 
	private String scrip_bnpl_enabled; 
	private String slice_flag; 
	private String delivery_buy_margin; 
	private String delivery_sell_margin; 
	private String mis_buy_margin; 
	private String mis_sell_margin; 
	private String mtf_buy_margin; 
	private String mtf_sell_margin; 
	private String boco_buy_margin; 
	private String boco_sell_margin;
}
