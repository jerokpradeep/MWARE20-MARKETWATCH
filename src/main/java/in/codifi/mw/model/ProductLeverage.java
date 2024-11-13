package in.codifi.mw.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductLeverage {

	@JsonProperty("delivery")
	private String delivery;
	@JsonProperty("intraday")
	private String intraday;
	@JsonProperty("bnpl")
	private String bnpl;
}
