package in.codifi.mw.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class Prompt {

	@JsonProperty("category")
	private String category;
	@JsonProperty("description")
	private String description;

}
