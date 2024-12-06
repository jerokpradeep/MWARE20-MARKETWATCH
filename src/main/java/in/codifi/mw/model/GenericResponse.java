/**
 * 
 */
package in.codifi.mw.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
public class GenericResponse {

	private String status;
	private String message;
	private Object result;
}
