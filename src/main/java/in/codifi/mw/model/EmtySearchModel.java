package in.codifi.mw.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmtySearchModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int pageCount;
	private int currentPage;
	private Object searchResult;

}
