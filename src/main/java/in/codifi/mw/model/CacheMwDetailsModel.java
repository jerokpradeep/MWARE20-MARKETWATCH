
package in.codifi.mw.model;

import java.io.Serializable;
import java.util.List;

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
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public int mwId;
	private static final long serialVersionUID = 1L;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public String mwName;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public String userId;
	public String exchange;
	public String segment;
	public String token;
	public String tradingSymbol;
	public String expiry;
	public int sortOrder;
	public String pdc;
	public String symbol;
	public String formattedInsName;
	private String weekTag;
	// New fields
	public badgeModel badge;
	public List<String> screeners;
}
