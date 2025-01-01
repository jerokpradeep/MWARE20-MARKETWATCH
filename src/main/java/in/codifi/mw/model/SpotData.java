package in.codifi.mw.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpotData {

	@JsonProperty("ltp")
	private Integer ltp;
	@JsonProperty("token")
	private String token;
	@JsonProperty("tradingSymbol")
	private String tradingSymbol;
	@JsonProperty("nsebseToken")
	private String nsebseToken;

}
