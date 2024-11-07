/**
 * 
 */
package in.codifi.mw.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "TBL_PRE_DEFINED_MARKET_WATCH_SCRIPS")
public class PredefinedMwScripsEntity implements Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "MW_ID")
	private int mwId;

	@Column(name = "TOKEN")
	private String token;

	@Column(name = "EXCH")
	private String exchange;

	@Column(name = "EXCH_SEG")
	private String segment;

	@Column(name = "SYMBOL")
	private String symbol;

	@Column(name = "TRADING_SYMBOL")
	private String tradingSymbol;

	@Column(name = "FORMATTED_INS_NAME")
	private String formattedInsName;

	@Column(name = "PDC")
	private String pdc;

	@Column(name = "LOT_SIZE")
	private String lotSize;

	@Column(name = "TICK_SIZE")
	private String tickSize;

	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "IST")
	@Column(name = "EXPIRY_DATE")
	private Date expiry;

	@Column(name = "WEEK_TAG")
	private String weekTag;

	@Column(name = "SORTING_ORDER")
	private int sortOrder;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "UPDATED_ON")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedOn;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "CREATED_BY")
	private String createdBy;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "ACTIVE_STATUS")
	private int activeStatus = 1;
}
