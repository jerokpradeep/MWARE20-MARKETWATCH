
package in.codifi.mw.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
public class MarketWatchNameDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int mwId;
	private String userId;
	private String mwName;
	private Long position;

}
