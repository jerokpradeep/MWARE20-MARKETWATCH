package in.codifi.mw.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecurityInfoRespModel {

	@JsonProperty("isin")
	private String isin;
	@JsonProperty("exchange")
	private String exchange;
	@JsonProperty("token")
	private String token;
	@JsonProperty("tradingSymbol")
	private String tradingSymbol;
	@JsonProperty("lotSize")
	private String lotSize;
	@JsonProperty("tickSize")
	private String tickSize;
	@JsonProperty("symbol")
	private String symbol;
	@JsonProperty("pdc")
	private String pdc;
	@JsonProperty("insType")
	private String insType;
	@JsonProperty("expiry")
	private Object expiry;
	@JsonProperty("qtyLimit")
	private String qtyLimit;
	@JsonProperty("sliceEnable")
	private String sliceEnable;
	@JsonProperty("surveillance")
	private String surveillance;
	@JsonProperty("scripIndex")
	private boolean scripIndex;
	@JsonProperty("isFnOAvailable")
	private boolean isFnOAvailable;
	@JsonProperty("spotData")
	private SpotData spotData;
	@JsonProperty("prompt")
	private List<Prompt> prompt;
	@JsonProperty("productLeverage")
	private ProductLeverage productLeverage;
	@JsonProperty("badge")
	private Badge badge;
	@JsonProperty("screeners")
	private List<String> screeners;
}
