/**
 * 
 */
package in.codifi.mw.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Vicky
 *
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponseModel {

	private String status;
	private String message;
	private String pageCount;
	private String currentPage;
	private Object result;
	private List<ScripSearchResp> searchResult;
}
