package in.codifi.mw.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class Badge {

	@JsonProperty("event")
	private String event;
	@JsonProperty("bnpl")
	private String bnpl;
	@JsonProperty("ideas")
	private String ideas;
	@JsonProperty("holdingqty")
	private Integer holdingqty;

}
