package in.codifi.mw.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;

import in.codifi.mw.entity.PredefinedMwEntity;
import in.codifi.mw.model.IndexModel;

/**
 * @author Vicky
 *
 */
public class MwCacheController {

	private static Map<String, List<JSONObject>> mwListByUserId = new ConcurrentHashMap<>();
	private static Map<String, List<JSONObject>> mwListUserId = new ConcurrentHashMap<>();
	private static Map<String, List<PredefinedMwEntity>> predefinedMwList = new ConcurrentHashMap<>();
	private static Map<String, List<PredefinedMwEntity>> masterPredefinedMwList = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, List<IndexModel>> getIndexData = new ConcurrentHashMap<>();

	/**
	 * @return the mwListUserId
	 */
	public static Map<String, List<JSONObject>> getMwListUserId() {
		return mwListUserId;
	}

	/**
	 * @return the getIndexData
	 */
	public static ConcurrentHashMap<String, List<IndexModel>> getGetIndexData() {
		return getIndexData;
	}

	/**
	 * @param getIndexData the getIndexData to set
	 */
	public static void setGetIndexData(ConcurrentHashMap<String, List<IndexModel>> getIndexData) {
		MwCacheController.getIndexData = getIndexData;
	}

	/**
	 * @param mwListUserId the mwListUserId to set
	 */
	public static void setMwListUserId(Map<String, List<JSONObject>> mwListUserId) {
		MwCacheController.mwListUserId = mwListUserId;
	}

	public static Map<String, List<JSONObject>> getMwListByUserId() {
		return mwListByUserId;
	}

	public static void setMwListByUserId(Map<String, List<JSONObject>> mwListByUserId) {
		MwCacheController.mwListByUserId = mwListByUserId;
	}

	public static Map<String, List<PredefinedMwEntity>> getPredefinedMwList() {
		return predefinedMwList;
	}

	public static void setPredefinedMwList(Map<String, List<PredefinedMwEntity>> predefinedMwList) {
		MwCacheController.predefinedMwList = predefinedMwList;
	}

	public static Map<String, List<PredefinedMwEntity>> getMasterPredefinedMwList() {
		return masterPredefinedMwList;
	}

	public static void setMasterPredefinedMwList(Map<String, List<PredefinedMwEntity>> masterPredefinedMwList) {
		MwCacheController.masterPredefinedMwList = masterPredefinedMwList;
	}


	
}
