/**
 * 
 */
package in.codifi.mw.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
@Entity(name = "TBL_SCRIP_MASTER_DETAILS")
public class ScripMasterEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private int id;

	@Column(name = "EXCHANGE_TYPE")
	private String exchange_type;

	@Column(name = "EXCH_SYMBOL")
	private String exch_symbol;

	@Column(name = "TRADING_SYMBOL")
	private String trading_symbol;

	@Column(name = "SERIES")
	private String series;

	@Column(name = "ISIN")
	private String isin;

	@Column(name = "INSTRUMENT_TYPE")
	private String instrument_type;

	@Column(name = "STRIKE_PRICE")
	private String strike_price;

	@Column(name = "OPTION_TYPE")
	private String option_type;

	@Column(name = "EXPIRY")
	private String expiry;

	@Column(name = "SCRIP_BNPL_ENABLED")
	private String scrip_bnpl_enabled;

	@Column(name = "SLICE_FLAG")
	private String slice_flag;

	@Column(name = "DELIVERY_BUY_MARGIN")
	private String delivery_buy_margin;

	@Column(name = "DELIVERY_SELL_MARGIN")
	private String delivery_sell_margin;

	@Column(name = "MIS_BUY_MARGIN")
	private String mis_buy_margin;
	
	@Column(name = "MIS_SELL_MARGIN")
	private String mis_sell_margin;

	@Column(name = "MTF_BUY_MARGIN")
	private String mtf_buy_margin;

	@Column(name = "MTF_SELL_MARGIN")
	private String mtf_sell_margin;

	@Column(name = "BOCO_BUY_MARGIN")
	private String boco_buy_margin;

	@Column(name = "BOCO_SELL_MARGIN")
	private String boco_sell_margin;

}
