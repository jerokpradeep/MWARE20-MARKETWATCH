/**
 * 
 */
package in.codifi.mw.model;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;
/**
 * @author Vicky
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"pageCount",
"currentPage",
"searchResult"
})
@Getter
@Setter
public class ResultModel {

	@JsonProperty("pageCount")
	private String pageCount;
	@JsonProperty("currentPage")
	private String currentPage;
	@JsonProperty("searchResult")
	private List<SearchResultModel> searchResult;
}
