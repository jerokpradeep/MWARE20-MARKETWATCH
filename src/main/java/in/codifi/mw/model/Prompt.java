package in.codifi.mw.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class Prompt implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("category")
	private String category;
	@JsonProperty("description")
	private String description;

}
