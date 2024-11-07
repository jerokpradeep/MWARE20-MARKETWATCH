
package in.codifi.mw.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Vicky
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseModel {

	private String status;
	private String message;
	private Object result;
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}
