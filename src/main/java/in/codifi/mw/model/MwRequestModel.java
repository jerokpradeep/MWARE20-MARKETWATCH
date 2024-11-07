
package in.codifi.mw.model;

import java.util.List;


/**
 * @author Vicky
 *
 */
public class MwRequestModel {

	private boolean predefined;
	private String userId;
	private int mwId;
	private String mwName;
	private List<MwScripModel> scripData;
	
	public boolean isPredefined() {
		return predefined;
	}

	public void setPredefined(boolean predefined) {
		this.predefined = predefined;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getMwId() {
		return mwId;
	}

	public void setMwId(int mwId) {
		this.mwId = mwId;
	}

	public String getMwName() {
		return mwName;
	}

	public void setMwName(String mwName) {
		this.mwName = mwName;
	}

	public List<MwScripModel> getScripData() {
		return scripData;
	}

	public void setScripData(List<MwScripModel> scripData) {
		this.scripData = scripData;
	}
}
