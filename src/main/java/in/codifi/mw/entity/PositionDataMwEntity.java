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

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */

@Getter
@Setter
@Entity(name = "TBL_POSITION_AVG_PRICE")
public class PositionDataMwEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	// Transient fields for extended data
		@Column(name = "USER_ID")
		private String userId;
		@Column(name = "EXCHANGE")
		private String exchange;
		@Column(name = "INSTRUMENT_TYPE")
		private String instrument_type;
		@Column(name = "SYMBOL")
		private String symbol;
		@Column(name = "EXPIRY")
		private String expiry;
		@Column(name = "STRIKE_PRICE")
		private String strike_price;
		@Column(name = "OPTION_TYPE")
		private String option_type;
		@Column(name = "INSTRUMENT_NAME")
		private String instrument_name;
		@Column(name = "NET_QTY")
		private String net_qty;
		@Column(name = "CF_BUY_QTY")
		private String cf_buy_qty;
		@Column(name = "CF_SELL_QTY")
		private String cf_sell_qty;
		@Column(name = "CLOSE_PRICE")
		private String close_price;
		@Column(name = "CF_BUY_AVG_RATE")
		private String cf_buy_avg_rate;
		@Column(name = "CF_SELL_AVG_RATE")
		private String cf_sell_avg_rate;
		@Column(name = "PRODUCT")
		private String product;
		@Column(name = "NET_RATE")
		private String net_rate;
		@Column(name = "TOKEN")
		private String token;

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
