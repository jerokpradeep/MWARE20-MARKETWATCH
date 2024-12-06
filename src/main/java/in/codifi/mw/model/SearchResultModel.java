/**
 * 
 */
package in.codifi.mw.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "exchange", "segment", "symbol", "token", "formattedInsName", "weekTag", "companyName", "expiry",
		"isFnOAvailable", "series", "isin" })
@Getter
@Setter
public class SearchResultModel {
	@JsonProperty("exchange")
	private String exchange;
	@JsonProperty("segment")
	private String segment;
	@JsonProperty("symbol")
	private String symbol;
	@JsonProperty("token")
	private String token;
	@JsonProperty("formattedInsName")
	private String formattedInsName;
	@JsonProperty("weekTag")
	private String weekTag;
	@JsonProperty("companyName")
	private String companyName;
	@JsonProperty("expiry")
	private Date expiry;
	@JsonProperty("isFnOAvailable")
	private String isFnOAvailable;
	@JsonProperty("series")
	private String series;
	@JsonProperty("isin")
	private String isin;
	@JsonProperty("optionType")
	private String optionType;
}
