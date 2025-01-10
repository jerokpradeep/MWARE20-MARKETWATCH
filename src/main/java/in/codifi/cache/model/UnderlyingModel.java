/**
 * 
 */
package in.codifi.cache.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
public class UnderlyingModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String exch;
	private String token;
	private String symbol;
	private String isin;
	private String lotSize;
	private String type;
	private String underlying;
	
}
