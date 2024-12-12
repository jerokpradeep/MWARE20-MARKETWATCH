
package in.codifi.mw.model;

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
public class MwRequestModel {
	@JsonProperty(value = "predefined")
	private boolean predefined;
	@JsonProperty(value = "mwId")
	private int mwId;
	@JsonProperty(value = "mwName")
	private String mwName;
	@JsonProperty(value = "isDefault")
	private boolean isDefault;
	@JsonProperty(value = "scripData")
	private List<MwScripModel> scripData;

}
