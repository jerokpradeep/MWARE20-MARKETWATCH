
package in.codifi.mw.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CacheMwDetailsModel implements Serializable {

	private static final long serialVersionUID = 1L;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public String mwName;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public String userId;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public int mwId;
	public String exchange;
	public String segment;
	public String token;
	public String tradingSymbol;
	public Date expiry;
	public int sortOrder;
	public String pdc;
	public String symbol;
	public String formattedInsName;
	private String weekTag;

	// New fields
	public Map<String, String> badge;
	public List<String> screeners;
}
