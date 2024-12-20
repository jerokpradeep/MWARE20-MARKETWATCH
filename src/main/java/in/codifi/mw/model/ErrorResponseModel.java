/**
 * 
 */
package in.codifi.mw.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ErrorResponseModel {
	@JsonProperty("status")
	private String status;
	@JsonProperty("message")
	private String message;
}
