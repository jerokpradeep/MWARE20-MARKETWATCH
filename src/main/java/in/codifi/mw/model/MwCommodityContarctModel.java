
package in.codifi.mw.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
public class MwCommodityContarctModel {
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public String token;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public String exchange;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public String segment;
	public String priceRange;
	public String priceUnit;
	public String QtyUnit;
	public String deliveryUnit;
	public String tickSize;
	public String lotSize;
	public String maxOrderValue;
	public String contractStartDate;
	public String tenderStartDate;
	public String tenderEndDate;
	public String delievryStartDate;
	public String delievryEndDate;
	public String lastTradingDate;

}
