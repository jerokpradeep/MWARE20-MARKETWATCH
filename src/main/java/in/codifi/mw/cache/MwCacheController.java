package in.codifi.mw.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;

import in.codifi.mw.entity.HoldingsDataMwEntity;
import in.codifi.mw.entity.HoldingsMwEntity;
import in.codifi.mw.entity.PositionDataMwEntity;
import in.codifi.mw.entity.PredefinedMwEntity;
import in.codifi.mw.model.IndexModel;

/**
 * @author Vicky
 *
 */
public class MwCacheController {

//	private static Map<String, List<JSONObject>> mwListByUserId = new ConcurrentHashMap<>();
//	private static Map<String, List<JSONObject>> mwListUserId = new ConcurrentHashMap<>();
//	private static Map<String, List<PredefinedMwEntity>> predefinedMwList = new ConcurrentHashMap<>();
//	private static Map<String, List<PredefinedMwEntity>> masterPredefinedMwList = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, List<JSONObject>> mwListByUserId = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, List<JSONObject>> mwListUserId = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, List<PredefinedMwEntity>> predefinedMwList = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, List<PredefinedMwEntity>> masterPredefinedMwList = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, List<IndexModel>> getIndexData = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, List<HoldingsDataMwEntity>> masterHoldingsMwList = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, List<PositionDataMwEntity>> masterPostionMwList = new ConcurrentHashMap<>();

	/**
	 * @return the mwListByUserId
	 */
	public static ConcurrentHashMap<String, List<JSONObject>> getMwListByUserId() {
		return mwListByUserId;
	}

	/**
	 * @param mwListByUserId the mwListByUserId to set
	 */
	public static void setMwListByUserId(ConcurrentHashMap<String, List<JSONObject>> mwListByUserId) {
		MwCacheController.mwListByUserId = mwListByUserId;
	}

	/**
	 * @return the mwListUserId
	 */
	public static ConcurrentHashMap<String, List<JSONObject>> getMwListUserId() {
		return mwListUserId;
	}

	/**
	 * @param mwListUserId the mwListUserId to set
	 */
	public static void setMwListUserId(ConcurrentHashMap<String, List<JSONObject>> mwListUserId) {
		MwCacheController.mwListUserId = mwListUserId;
	}

	/**
	 * @return the predefinedMwList
	 */
	public static ConcurrentHashMap<String, List<PredefinedMwEntity>> getPredefinedMwList() {
		return predefinedMwList;
	}

	/**
	 * @param predefinedMwList the predefinedMwList to set
	 */
	public static void setPredefinedMwList(ConcurrentHashMap<String, List<PredefinedMwEntity>> predefinedMwList) {
		MwCacheController.predefinedMwList = predefinedMwList;
	}

	/**
	 * @return the masterPredefinedMwList
	 */
	public static ConcurrentHashMap<String, List<PredefinedMwEntity>> getMasterPredefinedMwList() {
		return masterPredefinedMwList;
	}

	/**
	 * @param masterPredefinedMwList the masterPredefinedMwList to set
	 */
	public static void setMasterPredefinedMwList(
			ConcurrentHashMap<String, List<PredefinedMwEntity>> masterPredefinedMwList) {
		MwCacheController.masterPredefinedMwList = masterPredefinedMwList;
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
	 * @return the masterHoldingsMwList
	 */
	public static ConcurrentHashMap<String, List<HoldingsDataMwEntity>> getMasterHoldingsMwList() {
		return masterHoldingsMwList;
	}

	/**
	 * @param masterHoldingsMwList the masterHoldingsMwList to set
	 */
	public static void setMasterHoldingsMwList(
			ConcurrentHashMap<String, List<HoldingsDataMwEntity>> masterHoldingsMwList) {
		MwCacheController.masterHoldingsMwList = masterHoldingsMwList;
	}

	/**
	 * @return the masterPostionMwList
	 */
	public static ConcurrentHashMap<String, List<PositionDataMwEntity>> getMasterPostionMwList() {
		return masterPostionMwList;
	}

	/**
	 * @param masterPostionMwList the masterPostionMwList to set
	 */
	public static void setMasterPostionMwList(
			ConcurrentHashMap<String, List<PositionDataMwEntity>> masterPostionMwList) {
		MwCacheController.masterPostionMwList = masterPostionMwList;
	}

}
