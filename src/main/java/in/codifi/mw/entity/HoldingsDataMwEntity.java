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
@Entity(name = "TBL_HOLDINGS_DATA")
public class HoldingsDataMwEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	// Transient fields for extended data
	@Column(name = "USER_ID")
	private String userId;
	@Column(name = "HOLDINGS_TYPE")
	private String holdings_type;
	@Column(name = "ISIN")
	private String isin;
	@Column(name = "QTY")
	private String qty;
	@Column(name = "COLLATERAL_QTY")
	private String collateral_qty;
	@Column(name = "HAIRCUT")
	private String haircut;
	@Column(name = "BROKER_COLL_QTY")
	private String broker_coll_qty;
	@Column(name = "DP_QTY")
	private String dp_qty;
	@Column(name = "CLOSE_PRICE")
	private String close_price;
	@Column(name = "BEN_QTY")
	private String ben_qty;
	@Column(name = "BTST_QTY")
	private String btst_qty;
	@Column(name = "BTST_COLL_QTY")
	private String btst_coll_qty;
	@Column(name = "IS_EDIS")
	private String is_edis;
	@Column(name = "UNPLEDGE_QTY")
	private String unpledge_qty;
	@Column(name = "PRODUCT")
	private String product;
	@Column(name = "ACTUAL_PRICE")
	private String actual_price;

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
