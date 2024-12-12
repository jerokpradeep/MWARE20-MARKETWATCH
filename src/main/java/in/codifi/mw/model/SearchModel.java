package in.codifi.mw.model;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int pageCount;
	private int currentPage;
	private List<ScripSearchResp> searchResult;

}
