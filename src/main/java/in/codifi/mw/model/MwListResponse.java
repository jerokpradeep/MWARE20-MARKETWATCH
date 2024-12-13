package in.codifi.mw.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MwListResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
