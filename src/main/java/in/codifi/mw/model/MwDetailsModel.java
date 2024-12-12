package in.codifi.mw.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MwDetailsModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public int mwId;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	public String mwName;
	@JsonProperty(value = "isDefault")
	public boolean isDefault;
	@JsonProperty(value = "isRename")
	public boolean isRename;
	@JsonProperty(value = "isEdit")
	public boolean isEdit;
	private List<MwListResponse> scrip;
}
