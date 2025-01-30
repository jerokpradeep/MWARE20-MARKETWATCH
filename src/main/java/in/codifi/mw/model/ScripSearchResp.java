/**
 * 
 */
package in.codifi.mw.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
public class ScripSearchResp implements Serializable {

	private static final long serialVersionUID = 1L;
	@JsonProperty(value = "exchange")
	private String exchange = "";
	@JsonProperty(value = "segment")
	private String segment = "";
	@JsonProperty(value = "symbol")
	private String symbol = "";
	@JsonProperty(value = "token")
	private String token = "";
	@JsonProperty(value = "formattedInsName")
	private String formattedInsName = "";
	@JsonProperty(value = "weekTag")
	private String weekTag = "";
	@JsonProperty(value = "companyName")
	private String companyName = "";
	@JsonProperty(value = "expiry")
	private String expiry;
	@JsonProperty(value = "isFnOAvailable")
	private boolean isFnOAvailable;
	@JsonProperty(value = "series")
	private String series = "";
	@JsonProperty(value = "isin")
	private String isin = "";
	@JsonProperty(value = "optionType")
	private String optionType = "";
	@JsonProperty(value = "strike_price")
	private String strike_price = "";

}
