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
public class SearchScripReqModel {

	private String[] exchange;
	private String searchText;
	private String pageSize;
	private String currentPage;

}
